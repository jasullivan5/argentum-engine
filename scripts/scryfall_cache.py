"""Safe per-set cache filenames, automatically migrating legacy names on access."""

import os
from pathlib import Path
import re


CACHE_ROOT = Path.home() / ".cache" / "scryfall"


def _native_path(path: Path) -> str:
    # Extended paths let Windows rename a real legacy con.json without treating it
    # as the CON device. Do not open or stat that bare filename through Python.
    absolute = str(path.absolute())
    if os.name == "nt" and not absolute.startswith("\\\\?\\"):
        if absolute.startswith("\\\\"):
            return "\\\\?\\UNC\\" + absolute[2:]
        return "\\\\?\\" + absolute
    return absolute


def set_cache_path(code: str, root: Path = CACHE_ROOT) -> Path:
    """Return set-<code>.json, renaming a legacy entry without rewriting its data.

    Prefer an existing prefixed entry; never overwrite it with a legacy cache.
    The JSON schema and freshness rules remain unchanged.
    """
    code = code.lower()
    if not re.fullmatch(r"[a-z0-9]+", code):
        raise ValueError(f"Invalid set code: {code!r}")
    target = root / f"set-{code}.json"
    if target.exists() or not root.is_dir():
        return target
    # Enumerating avoids is_file()/exists() on reserved legacy names on Windows.
    for entry in root.iterdir():
        if entry.name.lower() == f"{code}.json":
            if os.path.isfile(_native_path(entry)):
                try:
                    os.rename(_native_path(entry), _native_path(target))
                except FileNotFoundError:
                    pass  # A concurrent run already migrated it.
            break
    return target
