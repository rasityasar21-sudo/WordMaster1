#!/usr/bin/env python3
"""Import the CC-BY-SA-4.0 English-Turkish StarDict release into WordMaster."""
import gzip
import html
import json
import pathlib
import re
import struct
import sys
import zipfile
from html.parser import HTMLParser

ASSET = pathlib.Path("app/src/main/assets/dictionary.json")
ZIP_PATH = pathlib.Path(sys.argv[1])
OUT = ASSET

class TextExtractor(HTMLParser):
    def __init__(self):
        super().__init__(convert_charrefs=True)
        self.parts = []
    def handle_data(self, data):
        value = data.strip()
        if value:
            self.parts.append(value)

def plain_text(raw):
    raw = raw.decode("utf-8", errors="replace") if isinstance(raw, bytes) else raw
    parser = TextExtractor()
    try:
        parser.feed(raw)
        text = " ".join(parser.parts)
    except Exception:
        text = re.sub(r"<[^>]+>", " ", raw)
    text = html.unescape(text)
    return re.sub(r"\s+", " ", text).strip()

def find_stardict(root):
    idxs = list(root.rglob("*.idx"))
    if not idxs:
        raise RuntimeError("ZIP içinden .idx sözlük dizini bulunamadı")
    for idx in idxs:
        candidates = [idx.with_suffix(".dict.dz"), idx.with_suffix(".dict")]
        dictionary = next((p for p in candidates if p.exists()), None)
        if dictionary:
            return idx, dictionary
    raise RuntimeError("Sözlük .idx bulundu ancak .dict.dz/.dict dosyası bulunamadı")

def parse_index(idx_path, dict_path):
    idx_bytes = idx_path.read_bytes()
    raw_dict = dict_path.read_bytes()
    if dict_path.name.endswith(".dz"):
        raw_dict = gzip.decompress(raw_dict)
    entries = []
    invalid_ranges = 0
    cursor = 0
    while cursor < len(idx_bytes):
        end = idx_bytes.find(b"\0", cursor)
        if end < 0 or end + 9 > len(idx_bytes):
            break
        word = idx_bytes[cursor:end].decode("utf-8", errors="replace").strip()
        offset, size = struct.unpack(">II", idx_bytes[end + 1:end + 9])
        cursor = end + 9
        if not word or offset + size > len(raw_dict):
            invalid_ranges += 1
            continue
        definition = plain_text(raw_dict[offset:offset + size])
        if not definition:
            continue
        entries.append({
            "en": word,
            "tr": definition[:1200],
            "level": "B1"
        })
    print(f"StarDict index={idx_path} ({len(idx_bytes)} bytes), dictionary={dict_path} ({len(raw_dict)} uncompressed bytes), invalid ranges={invalid_ranges}")
    return entries

def main():
    with zipfile.ZipFile(ZIP_PATH) as archive:
        archive.extractall("build/en-tr-dictionary")
    root = pathlib.Path("build/en-tr-dictionary")
    idx_path, dict_path = find_stardict(root)
    imported = parse_index(idx_path, dict_path)
    if len(imported) < 20000:
        raise RuntimeError(f"Beklenen sözlük sayısına ulaşılamadı: {len(imported)} kayıt; idx={idx_path}; dict={dict_path}")
    current = json.loads(ASSET.read_text(encoding="utf-8"))
    # Keep the existing hand-edited translations/examples when the headword already exists.
    merged = {}
    for item in imported:
        key = item["en"].casefold()
        merged[key] = item
    for item in current:
        key = item.get("en", "").casefold()
        if key:
            merged[key] = {**merged.get(key, {}), **item}
    result = sorted(merged.values(), key=lambda item: item.get("en", "").casefold())
    OUT.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    # Include the source's required attribution/license notices in the APK assets.
    notices = []
    for p in root.rglob("*"):
        if p.is_file() and p.name.lower() in {"license", "license.txt", "attribution", "attribution.txt", "readme.txt"}:
            try:
                notices.append(f"--- {p.name} ---\n" + p.read_text(encoding="utf-8", errors="replace"))
            except OSError:
                pass
    if notices:
        (ASSET.parent / "THIRD_PARTY_DICTIONARY_ATTRIBUTION.txt").write_text(
            "Source: DanielGregorini/koreader-dicts, en-tr. Distributed under CC-BY-SA-4.0.\n\n" + "\n\n".join(notices),
            encoding="utf-8"
        )
    print(f"Imported {len(imported)} source entries; bundled total after merge: {len(result)}")

if __name__ == "__main__":
    main()
