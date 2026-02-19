"""
replace_logos.py — Replace all Zashi branding assets with Zol Wallet assets.

Part 1: Convert Android.png launcher icons to WebP for all mipmap densities.
Part 2: Write Android XML vector drawables for all branding drawable targets.
"""

import os
import sys
from pathlib import Path

# ---------------------------------------------------------------------------
# Paths
# ---------------------------------------------------------------------------

PROJECT = Path(r"C:\Users\thomas\StudioProjects\zolwallet-android")

ANDROID_PNG   = PROJECT / "docs/brand/Logo Files/Favicons/Android.png"
BLACK_LOGO    = PROJECT / "docs/brand/Logo Files/png/Black logo - no background.png"
WHITE_LOGO    = PROJECT / "docs/brand/Logo Files/png/White logo - no background.png"

MIPMAP_BASE   = PROJECT / "ui-lib/src/main/res/ui/common"
DRAWABLE_BASE = PROJECT / "ui-design-lib/src/main/res/ui/common"

# ---------------------------------------------------------------------------
# Path data constants
# ---------------------------------------------------------------------------

ICON_PATH = (
    "M-167.5,390.5c-1.1,0-2-0.9-2-2c0-1.1,0.9-2,2-2c1.1,0,2,0.9,2,2"
    "C-165.5,389.6-166.4,390.5-167.5,390.5z M-177.5,428.5c-2.2,0-4-1.8-4-4"
    "s1.8-4,4-4c2.2,0,4,1.8,4,4S-175.3,428.5-177.5,428.5z M-177.5,410.5"
    "c-2.2,0-4-1.8-4-4s1.8-4,4-4c2.2,0,4,1.8,4,4S-175.3,410.5-177.5,410.5z"
    " M-177.5,392.5c-2.2,0-4-1.8-4-4c0-2.2,1.8-4,4-4c2.2,0,4,1.8,4,4"
    "C-173.5,390.7-175.3,392.5-177.5,392.5z M-177.5,374.5c-2.2,0-4-1.8-4-4"
    "c0-2.2,1.8-4,4-4c2.2,0,4,1.8,4,4C-173.5,372.7-175.3,374.5-177.5,374.5z"
    " M-194.5,414.5c-3.9,0-7-3.1-7-7c0-3.9,3.1-7,7-7c3.9,0,7,3.1,7,7"
    "C-187.5,411.4-190.6,414.5-194.5,414.5z M-194.5,394.5c-3.9,0-7-3.1-7-7"
    "c0-3.9,3.1-7,7-7c3.9,0,7,3.1,7,7C-187.5,391.4-190.6,394.5-194.5,394.5z"
    " M-195.5,374.5c-2.2,0-4-1.8-4-4c0-2.2,1.8-4,4-4c2.2,0,4,1.8,4,4"
    "C-191.5,372.7-193.3,374.5-195.5,374.5z M-195.5,362.5c-1.1,0-2-0.9-2-2"
    "c0-1.1,0.9-2,2-2c1.1,0,2,0.9,2,2C-193.5,361.6-194.4,362.5-195.5,362.5z"
    " M-214.5,414.5c-3.9,0-7-3.1-7-7c0-3.9,3.1-7,7-7s7,3.1,7,7"
    "C-207.5,411.4-210.6,414.5-214.5,414.5z M-214.5,394.5c-3.9,0-7-3.1-7-7"
    "c0-3.9,3.1-7,7-7s7,3.1,7,7C-207.5,391.4-210.6,394.5-214.5,394.5z"
    " M-213.5,374.5c-2.2,0-4-1.8-4-4c0-2.2,1.8-4,4-4c2.2,0,4,1.8,4,4"
    "C-209.5,372.7-211.3,374.5-213.5,374.5z M-213.5,362.5c-1.1,0-2-0.9-2-2"
    "c0-1.1,0.9-2,2-2c1.1,0,2,0.9,2,2C-211.5,361.6-212.4,362.5-213.5,362.5z"
    " M-231.5,374.5c-2.2,0-4-1.8-4-4c0-2.2,1.8-4,4-4c2.2,0,4,1.8,4,4"
    "C-227.5,372.7-229.3,374.5-231.5,374.5z M-231.5,384.5c2.2,0,4,1.8,4,4"
    "c0,2.2-1.8,4-4,4c-2.2,0-4-1.8-4-4C-235.5,386.3-233.7,384.5-231.5,384.5z"
    " M-241.5,408.5c-1.1,0-2-0.9-2-2c0-1.1,0.9-2,2-2c1.1,0,2,0.9,2,2"
    "C-239.5,407.6-240.4,408.5-241.5,408.5z M-241.5,390.5c-1.1,0-2-0.9-2-2"
    "c0-1.1,0.9-2,2-2c1.1,0,2,0.9,2,2C-239.5,389.6-240.4,390.5-241.5,390.5z"
    " M-231.5,402.5c2.2,0,4,1.8,4,4s-1.8,4-4,4c-2.2,0-4-1.8-4-4"
    "S-233.7,402.5-231.5,402.5z M-231.5,420.5c2.2,0,4,1.8,4,4s-1.8,4-4,4"
    "c-2.2,0-4-1.8-4-4S-233.7,420.5-231.5,420.5z M-213.5,420.5c2.2,0,4,1.8,4,4"
    "c0,2.2-1.8,4-4,4c-2.2,0-4-1.8-4-4C-217.5,422.3-215.7,420.5-213.5,420.5z"
    " M-213.5,432.5c1.1,0,2,0.9,2,2c0,1.1-0.9,2-2,2c-1.1,0-2-0.9-2-2"
    "C-215.5,433.4-214.6,432.5-213.5,432.5z M-195.5,420.5c2.2,0,4,1.8,4,4"
    "c0,2.2-1.8,4-4,4c-2.2,0-4-1.8-4-4C-199.5,422.3-197.7,420.5-195.5,420.5z"
    " M-195.5,432.5c1.1,0,2,0.9,2,2c0,1.1-0.9,2-2,2c-1.1,0-2-0.9-2-2"
    "C-197.5,433.4-196.6,432.5-195.5,432.5z M-167.5,404.5c1.1,0,2,0.9,2,2"
    "c0,1.1-0.9,2-2,2c-1.1,0-2-0.9-2-2C-169.5,405.4-168.6,404.5-167.5,404.5z"
)

WORDMARK_PATH = (
    "M11.06 8.9 l-6.64 8.32 l6.76 0 l0 2.78 l-10.64 0 l0 -2.56 l6.62 -8.36"
    " l-6.6 0 l0 -2.78 l10.5 0 l0 2.6 z"
    " M17.64 9.54 c3.16 0 5.52 2.36 5.52 5.36 c0 2.98 -2.36 5.34 -5.52 5.34"
    " s-5.5 -2.34 -5.5 -5.34 c0 -2.98 2.36 -5.36 5.5 -5.36 z"
    " M17.64 12.06 c-1.56 0 -2.76 1.2 -2.76 2.84 s1.14 2.86 2.7 2.86"
    " s2.82 -1.22 2.82 -2.86 s-1.2 -2.84 -2.76 -2.84 z"
    " M26.86 20 l-2.8 0 l0 -13.7 l2.8 0 l0 13.7 z"
)

# ---------------------------------------------------------------------------
# XML template helpers
# ---------------------------------------------------------------------------

XML_HEADER = '<?xml version="1.0" encoding="utf-8"?>'

VECTOR_OPEN = (
    '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
    '    android:width="{width}dp"\n'
    '    android:height="{height}dp"\n'
    '    android:viewportWidth="{vw}"\n'
    '    android:viewportHeight="{vh}">'
)

VECTOR_CLOSE = "</vector>"

GROUP_ICON = (
    '    <group\n'
    '        android:scaleX="10.034"\n'
    '        android:scaleY="10.034"\n'
    '        android:translateX="{tx}"\n'
    '        android:translateY="{ty}">\n'
    '        <path\n'
    '            android:pathData="{path}"\n'
    '            android:fillColor="{color}" />\n'
    '    </group>'
)

GROUP_WORDMARK = (
    '    <group\n'
    '        android:scaleX="74.34"\n'
    '        android:scaleY="74.34"\n'
    '        android:translateX="{tx}"\n'
    '        android:translateY="{ty}">\n'
    '        <path\n'
    '            android:pathData="{path}"\n'
    '            android:fillColor="{color}" />\n'
    '    </group>'
)


def make_icon_group(tx, ty, color):
    return GROUP_ICON.format(tx=tx, ty=ty, path=ICON_PATH, color=color)


def make_wordmark_group(tx, ty, color):
    return GROUP_WORDMARK.format(tx=tx, ty=ty, path=WORDMARK_PATH, color=color)


def make_xml(width, height, vw, vh, body_lines):
    header = XML_HEADER
    vector_open = VECTOR_OPEN.format(width=width, height=height, vw=vw, vh=vh)
    body = "\n".join(body_lines)
    return f"{header}\n{vector_open}\n{body}\n{VECTOR_CLOSE}\n"


# ---------------------------------------------------------------------------
# Drawable definitions
# ---------------------------------------------------------------------------
# Each entry: (relative_path, width, height, viewport_type, color)
# viewport_type: "icon" | "wordmark" | "full"
# ---------------------------------------------------------------------------

COLOR_LIGHT = "#231F20"
COLOR_DARK  = "#E8E8E8"

DRAWABLES = [
    # (relative path from DRAWABLE_BASE, w, h, type, color)
    ("drawable/ic_app_bar_zashi.xml",              60, 20, "full",     COLOR_LIGHT),
    ("drawable-night/ic_app_bar_zashi.xml",        60, 20, "full",     COLOR_DARK),
    ("drawable/zashi_text_logo.xml",              100, 53, "wordmark", COLOR_LIGHT),
    ("drawable/zashi_text_logo_small.xml",         64, 34, "wordmark", COLOR_LIGHT),
    ("drawable/zashi_logo_without_text.xml",       36, 36, "icon",     COLOR_LIGHT),
    ("drawable-night/zashi_logo_without_text.xml", 36, 36, "icon",     COLOR_DARK),
    ("drawable/ic_app_bar_zashi_icon.xml",         24, 24, "icon",     COLOR_LIGHT),
    ("drawable/ic_item_zashi.xml",                 40, 40, "icon",     COLOR_LIGHT),
    ("drawable-night/ic_item_zashi.xml",           40, 40, "icon",     COLOR_DARK),
    ("drawable/img_zashi_version.xml",             75, 26, "full",     COLOR_LIGHT),
    ("drawable-night/img_zashi_version.xml",       75, 26, "full",     COLOR_DARK),
]

VIEWPORT_DIMS = {
    "icon":     (783,  783),
    "wordmark": (1957, 1037),
    "full":     (3001, 1037),
}

# Transform values per viewport type and content type within full
TRANSFORMS = {
    "icon": {
        "icon": {"tx": "2443.3", "ty": "-3597.2"},
    },
    "wordmark": {
        "wordmark": {"tx": "-40.1", "ty": "-468.3"},
    },
    "full": {
        "icon":     {"tx": "2443.3", "ty": "-3466.4"},
        "wordmark": {"tx": "1003.9", "ty": "-468.3"},
    },
}


def build_body(viewport_type, color):
    if viewport_type == "icon":
        t = TRANSFORMS["icon"]["icon"]
        return [make_icon_group(t["tx"], t["ty"], color)]
    elif viewport_type == "wordmark":
        t = TRANSFORMS["wordmark"]["wordmark"]
        return [make_wordmark_group(t["tx"], t["ty"], color)]
    elif viewport_type == "full":
        ti = TRANSFORMS["full"]["icon"]
        tw = TRANSFORMS["full"]["wordmark"]
        return [
            make_icon_group(ti["tx"], ti["ty"], color),
            make_wordmark_group(tw["tx"], tw["ty"], color),
        ]
    else:
        raise ValueError(f"Unknown viewport_type: {viewport_type}")


# ---------------------------------------------------------------------------
# Part 2: Write XML vector drawables
# ---------------------------------------------------------------------------

def write_xml_drawables():
    print("\n=== Part 2: XML Vector Drawables ===\n")

    # Determine which night-variant files already exist on disk
    night_dir = DRAWABLE_BASE / "drawable-night"
    existing_night = set()
    if night_dir.is_dir():
        for f in night_dir.iterdir():
            if f.is_file():
                existing_night.add(f.name)

    written = []
    skipped = []

    for rel_path, w, h, vtype, color in DRAWABLES:
        dest = DRAWABLE_BASE / rel_path
        is_night = rel_path.startswith("drawable-night/")
        fname = dest.name

        # For night variants, only write if the file already exists
        if is_night and fname not in existing_night:
            print(f"  SKIP (night variant not pre-existing): {rel_path}")
            skipped.append(rel_path)
            continue

        vw, vh = VIEWPORT_DIMS[vtype]
        body = build_body(vtype, color)
        xml_content = make_xml(w, h, vw, vh, body)

        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_text(xml_content, encoding="utf-8")
        print(f"  WROTE: {rel_path}")
        written.append(str(dest))

    return written, skipped


# ---------------------------------------------------------------------------
# Part 1: WebP launcher icons
# ---------------------------------------------------------------------------

LAUNCHER_SIZES = {
    "mdpi":    48,
    "hdpi":    72,
    "xhdpi":   96,
    "xxhdpi":  144,
    "xxxhdpi": 192,
}

FOREGROUND_SIZES = {
    "mdpi":    81,
    "hdpi":    108,
    "xhdpi":   144,
    "xxhdpi":  216,
    "xxxhdpi": 288,
}


def remove_background(img, tolerance=30):
    """Return a copy of img (RGBA) with the background color made transparent.

    The background color is sampled from the top-left pixel.
    All pixels within *tolerance* (Euclidean distance in RGB) of that color
    are replaced with fully-transparent pixels.
    """
    img = img.convert("RGBA")
    data = img.load()
    width, height = img.size

    # Sample background from top-left corner
    bg_r, bg_g, bg_b, _ = data[0, 0]

    for y in range(height):
        for x in range(width):
            r, g, b, a = data[x, y]
            dist = ((r - bg_r) ** 2 + (g - bg_g) ** 2 + (b - bg_b) ** 2) ** 0.5
            if dist <= tolerance:
                data[x, y] = (r, g, b, 0)

    return img


def write_webp_icons():
    print("\n=== Part 1: WebP Launcher Icons ===\n")

    try:
        from PIL import Image
    except ImportError:
        print("ERROR: Pillow not installed. Run: pip install Pillow")
        return [], ["Pillow not available"]

    if not ANDROID_PNG.exists():
        print(f"ERROR: Source not found: {ANDROID_PNG}")
        return [], [str(ANDROID_PNG)]

    src = Image.open(ANDROID_PNG).convert("RGBA")
    bg_pixel = src.getpixel((0, 0))
    print(f"  Source: {ANDROID_PNG.name}  size={src.size}  bg_pixel={bg_pixel[:3]}")

    foreground_src = remove_background(src)

    written = []
    errors = []

    # ic_launcher and ic_launcher_round (square, opaque/cream bg, lossy q=95)
    for density, size in LAUNCHER_SIZES.items():
        for name in ("ic_launcher.webp", "ic_launcher_round.webp"):
            dest = MIPMAP_BASE / f"mipmap-{density}" / name
            if not dest.parent.exists():
                print(f"  SKIP (dir missing): mipmap-{density}/{name}")
                errors.append(f"dir missing: mipmap-{density}")
                continue
            resized = src.resize((size, size), Image.LANCZOS)
            # WebP with alpha needs RGBA; lossy WebP supports RGBA in Pillow
            resized.save(str(dest), format="WEBP", quality=95)
            print(f"  WROTE: mipmap-{density}/{name}  ({size}x{size})")
            written.append(str(dest))

    # ic_launcher_foreground (transparent bg, lossless)
    for density, size in FOREGROUND_SIZES.items():
        dest = MIPMAP_BASE / f"mipmap-{density}" / "ic_launcher_foreground.webp"
        if not dest.parent.exists():
            print(f"  SKIP (dir missing): mipmap-{density}/ic_launcher_foreground.webp")
            errors.append(f"dir missing: mipmap-{density}")
            continue
        resized = foreground_src.resize((size, size), Image.LANCZOS)
        resized.save(str(dest), format="WEBP", lossless=True)
        print(f"  WROTE: mipmap-{density}/ic_launcher_foreground.webp  ({size}x{size})")
        written.append(str(dest))

    return written, errors


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main():
    print("=== replace_logos.py — Zol Wallet branding replacement ===")
    print(f"  Project: {PROJECT}")

    webp_written, webp_errors = write_webp_icons()
    xml_written, xml_skipped  = write_xml_drawables()

    print("\n=== Summary ===\n")

    print(f"WebP launcher icons written : {len(webp_written)}")
    for p in webp_written:
        print(f"  + {Path(p).relative_to(PROJECT)}")

    if webp_errors:
        print(f"\nWebP errors/skips          : {len(webp_errors)}")
        for e in webp_errors:
            print(f"  ! {e}")

    print(f"\nXML drawables written      : {len(xml_written)}")
    for p in xml_written:
        print(f"  + {Path(p).relative_to(PROJECT)}")

    if xml_skipped:
        print(f"\nXML night variants skipped : {len(xml_skipped)}")
        for s in xml_skipped:
            print(f"  - {s}  (file did not pre-exist, not created)")

    print("\nDone.")


if __name__ == "__main__":
    main()
