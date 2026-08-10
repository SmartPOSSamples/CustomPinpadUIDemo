# CustomPinpadUIDemo

A **custom PIN Pad keyboard layout** solution that works as a pair of tools:

| Tool | Language / Platform | Purpose |
| --- | --- | --- |
| `pinpad_marker.py` | Python 3 + tkinter | Offline annotation tool: draw lines on a keyboard background image and export a `.tlv` keyboard-layout file |
| `app/` (Android) | Java + Gradle | On-device demo: pick a TLV / draw lines to generate TLV, push it to the PINPad device and enter a PIN |

> Official layout parameters are documented in `app/docs/Customized PINPad Layout Parameters.pdf`.

---

## 1. TLV Binary Format (shared by both tools)

Each TLV item consists of **tag (1 byte) + length (4 bytes big-endian) + value**:

```
+------+----------------+-------------+
| tag  |  length(4B BE) |   value     |
+------+----------------+-------------+
```

| tag | Meaning |
| --- | --- |
| `0x11` | UI area location `left,top,right,bottom`, each 2 bytes big-endian = 8 bytes |
| `0x12` | Keyboard background image (PNG bitmap data) |
| `0x13` | Input-text location |
| `0x20–0x29` | Locations of digit keys 0–9 |
| `0x2A` | OK (Enter) location |
| `0x2B` | CLEAR (Backspace) location |
| `0x2C` | CANCEL location |
| `0x30` | Key-tone switch |
| `0x31` | Background color, e.g. `"#FFFFFF"` |
| `0x32` | Text color, e.g. `"#000000"` |

Build side: Python `generate_tlvs_bytes()` and Java `TlvGenerator`. Parse side: `DataParser` / `TlvParser`. The output of either side is fully interchangeable with the other.

### Coordinate semantics

- **Keys** (`0x20–0x29`, and OK `0x2A` / CLEAR `0x2B`): locations are **relative to the keyboard image** (the UI area sits at the bottom of the screen). At build time (`getLocRelativeToUi`) they are converted to full-screen coordinates by adding the UI offset.
- **CANCEL** (`0x2C`): location is **absolute** — measured from the **top-left corner of the screen** (default `[0, 0, 190, 100]`) and is NOT offset by the UI position.

---

## 2. Python Annotation Tool `pinpad_marker.py`

A Python replica of `ImageLineView.java` (desktop line-marking tool).

### Requirements

Python 3 + `Pillow` + `tkinter` (works on Windows / macOS / Linux).

```bash
pip install Pillow
```

### Usage

```bash
python pinpad_marker.py                  # start with no image; file-picker opens automatically
python pinpad_marker.py <image_path>     # load a keyboard background image directly
python pinpad_marker.py --demo           # no GUI; prints only the algorithm result
```

### Operation flow

1. **Pick an image**: on startup, choose the PIN Pad keyboard background to display.
2. **Set key size**: `W` / `H` in the toolbar (default 132×70), then click **Apply** (lines confirmed afterwards are spaced by the new size).
3. **Mark lines**: drag the yellow active line to the target position, or fine-tune with the arrow keys (`Shift+arrow` steps 10px); tap the green **✓** to confirm. Confirm **4 horizontal lines**, then the mode switches to **_3 vertical lines_** automatically.
4. **Set screen size**: `ScreenW` / `ScreenH` in the toolbar (default 480×480), used to compute the UI offset in the TLV.
5. **Export**: after all 7 lines are confirmed, click **Export TLV** and save a `.tlv` binary file.

A magnifier helps precise placement: a 3× zoom with a reference line is shown while dragging.

### Output

A single `.tlv` file conforming to Section 1's format: the keyboard image plus location rectangles for keys 0–9 and OK/CLEAR/CANCEL (already mapped to full-screen coordinates).

---

## 3. Android Demo App

### Main screen (MainActivity) buttons

| Button | Action |
| --- | --- |
| **Example** | Shows a TLV picker (`gui.tlv` / `q3mu.tlv` / `q2.tlv` / `q21.tlv` / default) → enters `InputPINActivity` after selection |
| **DebugKeyboardTlvs** | Pops a local preview UI of the default keyboard TLV (no physical PINPad needed) |
| **ViewImage** | Opens `ImageViewActivity`: the on-device line-marking tool (`ImageLineView`), same flow as the Python tool |
| **Clear_Logs** | Clears the log area |

### Byte-array loading in InputPINActivity

`startInputPIN()` fills the `tlvs` array by priority:

1. A raw TLV has been selected (`RawTlvSource.selectedRawResId != 0`) → `readRawBytes()` reads the chosen file
2. Otherwise, arrays from ViewImage (`keyLocs`) → generate the TLV
3. Otherwise, use the default keyboard TLV

It then calls `device.setGUIConfiguration(6, tlvs)` to push the layout to the PINPad and `waitForPinBlock()` to await PIN entry.

### Selectable TLV list (`RawTlvSource.java`)

```java
RAW_TLV_RES_IDS = { R.raw.gui, R.raw.q3mu, R.raw.q2, R.raw.q21, 0 /*default*/ };
RAW_TLV_NAMES   = { "gui.tlv", "q3mu.tlv", "q2.tlv", "q21.tlv", "default (generated)" };
```

The lists are index-aligned; `0` means "don't use a file, use the generation logic".

---

## 4. How the Two Tools Work Together

### Option A: offline annotation → TLV → install into the app

```
1. Draw lines with the Python tool and export xx.tlv
        ↓
2. Put xx.tlv into app/src/main/res/raw/ (resource name = filename without extension)
        ↓
3. Add an entry to RAW_TLV_RES_IDS and RAW_TLV_NAMES in RawTlvSource.java
        ↓
4. Rebuild and reinstall the app; tap Example on the main screen → select that entry
        ↓
5. InputPINActivity fills tlvs from that file and pushes it via setGUIConfiguration
```

### Option B: draw lines directly on the device (no Python needed)

Tap **ViewImage** on the main screen → draw 4 horizontal + 3 vertical lines on the image as usual → tap **Show Pinpad** → the TLV is generated and InputPINActivity is launched with it.

> Both options use exactly the same line-marking algorithm (4 horizontal + 3 vertical lines, default key size 132×70, producing identical keyLocs). You can iterate on the layout offline on your PC and, once tuned, bake it into `res/raw`.

---

## 5. Building the App

```bash
# Requires the Android SDK (compileSdk 23); run from the project root:
gradlew assembleDebug
# or just verify the sources:
gradlew compileDebugJavaWithJavac
```