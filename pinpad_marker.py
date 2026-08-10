"""
Python replica of ImageLineView.java (Q3MU annotation tool) with a tkinter GUI.

Usage:
  python pinpad_marker.py                       no image loaded (choose via dialog)
  python pinpad_marker.py <image_path>          custom image
  python pinpad_marker.py --demo                print-only algorithm demo (no GUI)
"""

import sys
import math

try:
    import tkinter as tk
    from PIL import Image, ImageTk
    _HAS_GUI = True
except Exception:
    _HAS_GUI = False

MODE_HORIZONTAL = 0
MODE_VERTICAL = 1
MODE_DONE = 2

ORIENTATION_HORIZONTAL = 0
ORIENTATION_VERTICAL = 1

HORIZONTAL_MAX = 4
VERTICAL_MAX = 3

LINE_TOUCH_RADIUS = 60
MAGNIFIER_ZOOM = 3.0
MAGNIFIER_SIZE = 200.0

DEFAULT_SIZE_WIDTH = 132
DEFAULT_SIZE_HEIGHT = 70


class PinLine:
    def __init__(self, position, orientation):
        self.position = position
        self.orientation = orientation
        self.locked = False

    def set_locked(self, locked):
        self.locked = locked


class ImageMarker:
    def __init__(self, bmp_width, bmp_height, size_width=DEFAULT_SIZE_WIDTH, size_height=DEFAULT_SIZE_HEIGHT):
        self.bmp_w = bmp_width
        self.bmp_h = bmp_height
        self.size_width = size_width
        self.size_height = size_height

        self.active_line = PinLine(80.0, ORIENTATION_HORIZONTAL)
        self.locked_lines = []
        self.row_pairs = []
        self.col_pairs = []

        self.line_mode = MODE_HORIZONTAL
        self.horizontal_confirmed = 0
        self.vertical_confirmed = 0

        self.generated_key_locs = None
        self.generated_function_key_locs = None

    def set_input_size(self, width, height):
        self.size_width = width
        self.size_height = height

    def set_active_position(self, value):
        if self.active_line is None or self.active_line.locked:
            return
        if self.active_line.orientation == ORIENTATION_HORIZONTAL:
            self.active_line.position = min(max(value, 0), self.bmp_h)
        else:
            self.active_line.position = min(max(value, 0), self.bmp_w)

    def confirm_current_line(self):
        if self.active_line is None:
            return -1

        if self.active_line.orientation == ORIENTATION_HORIZONTAL:
            self.horizontal_confirmed += 1
            cur_y = self.active_line.position
            first = PinLine(cur_y, ORIENTATION_HORIZONTAL)
            first.set_locked(True)
            self.locked_lines.append(first)
            second_y = min(cur_y + self.size_height, self.bmp_h)
            second = PinLine(second_y, ORIENTATION_HORIZONTAL)
            second.set_locked(True)
            self.locked_lines.append(second)
            self.row_pairs.append([int(cur_y), int(second_y)])

            if self.horizontal_confirmed >= HORIZONTAL_MAX:
                self.line_mode = MODE_VERTICAL
                self.active_line = PinLine(80.0, ORIENTATION_VERTICAL)
            else:
                self.active_line = PinLine(min(cur_y + self.size_height * 2, self.bmp_h), ORIENTATION_HORIZONTAL)
        else:
            self.vertical_confirmed += 1
            cur_pos = self.active_line.position
            first = PinLine(cur_pos, ORIENTATION_VERTICAL)
            first.set_locked(True)
            self.locked_lines.append(first)
            second_pos = min(cur_pos + self.size_width, self.bmp_w)
            second = PinLine(second_pos, ORIENTATION_VERTICAL)
            second.set_locked(True)
            self.locked_lines.append(second)
            self.col_pairs.append([int(cur_pos), int(second_pos)])

            if self.vertical_confirmed >= VERTICAL_MAX:
                self.line_mode = MODE_DONE
                self.active_line = None
                self.generate_key_arrays()
            else:
                self.active_line = PinLine(min(cur_pos + self.size_width * 2, self.bmp_w), ORIENTATION_VERTICAL)

        return self.active_line.position if self.active_line else -1

    def generate_key_arrays(self):
        if len(self.row_pairs) < 4 or len(self.col_pairs) < 3:
            return
        row0, row1, row2, row3 = self.row_pairs[:4]
        col0, col1, col2 = self.col_pairs[:3]

        key_locs = [[0, 0, 0, 0] for _ in range(10)]
        key_locs[0] = [col1[0], row3[0], col1[1], row3[1]]
        for i in range(1, 10):
            r = (i - 1) // 3
            c = (i - 1) % 3
            col = self.col_pairs[c]
            row = self.row_pairs[r]
            key_locs[i] = [col[0], row[0], col[1], row[1]]

        function_key_locs = [None, None, None]
        function_key_locs[0] = [col0[0], row3[0], col0[1], row3[1]]  # Enter
        function_key_locs[1] = [col2[0], row3[0], col2[1], row3[1]]  # BackSpace

        self.generated_key_locs = key_locs
        self.generated_function_key_locs = function_key_locs
        return key_locs, function_key_locs


def get_loc_relative_to_ui(key_locs, ui_loc):
    if key_locs is None or ui_loc is None or len(ui_loc) < 2:
        return key_locs
    off_x, off_y = ui_loc[0], ui_loc[1]
    result = [None] * len(key_locs)
    for i, loc in enumerate(key_locs):
        if loc is None:
            break
        result[i] = [loc[0] + off_x, loc[1] + off_y, loc[2] + off_x, loc[3] + off_y]
    return result


def get_tlvs_from_arrays(screen_size, img_height, key_locs, function_key_locs):
    ui_loc = [0, screen_size[1] - img_height, screen_size[0], screen_size[1]]
    func = get_loc_relative_to_ui(function_key_locs, ui_loc)
    func[2] = [0, 0, 190, 100]
    keys = get_loc_relative_to_ui(key_locs, ui_loc)
    return keys, func



TAG_UI_LOC = 0x11
TAG_UI = 0x12
TAG_TEXT_LOC = 0x13
TAG_K0 = 0x20          # 0x20..0x29 -> keys 0..9
TAG_OK = 0x2A          # 0x2A OK / 0x2B CLEAR / 0x2C CANCEL
TAG_CLEAR = 0x2B
TAG_CANCEL = 0x2C
TAG_SOUND = 0x30
TAG_BG = 0x31
TAG_TG = 0x32


def int2byte2(value):
    """Mirror ByteConvert.int2byte2(value) big endian."""
    return bytes([(value >> 8) & 0xFF, value & 0xFF])


def create_location_value(locs):
    """left, top, right, bottom each as 2-byte big endian (8 bytes)."""
    out = bytearray()
    for v in locs:
        out += int2byte2(v)
    return bytes(out)


def build_tlv(tag, value):
    """Mirror TlvPacker.buildTLV: 1-byte tag + 4-byte length (big endian) + value."""
    value = value if value is not None else b""
    return bytes([tag]) + len(value).to_bytes(4, "big") + value


def generate_tlvs_bytes(image, ui_loc, keys, function_keys):
    """Mirror TlvGenerator.generateTlvs(). `image` is a PIL.Image."""
    import io
    buf = io.BytesIO()
    image.save(buf, format="PNG")
    bitmap_data = buf.getvalue()

    tlvs = [build_tlv(TAG_UI_LOC, create_location_value(ui_loc)),
            build_tlv(TAG_UI, bitmap_data)]

    for i in range(10):
        tlvs.append(build_tlv(TAG_K0 + i, create_location_value(keys[i])))

    for i in range(3):
        tlvs.append(build_tlv(TAG_OK + i, create_location_value(function_keys[i])))

    tlvs.append(build_tlv(TAG_SOUND, bytes([0x01])))
    tlvs.append(build_tlv(TAG_BG, b"#FFFFFF"))
    tlvs.append(build_tlv(TAG_TG, b"#000000"))

    out = b""
    for t in tlvs:
        out += t
    return out


def export_tlvs_hex_file(image, ui_loc, keys, function_keys, out_path):
    """Build final TLV bytes and write binary file. Returns (len(data))."""
    data = generate_tlvs_bytes(image, ui_loc, keys, function_keys)
    with open(out_path, "wb") as f:
        f.write(data)
    return len(data)


def demo():
    marker = ImageMarker(480, 347)
    for p in (90, 172, 254, 336):
        marker.set_active_position(p)
        marker.confirm_current_line()
    for p in (80, 164, 248):
        marker.set_active_position(p)
        marker.confirm_current_line()
    print("line_mode = %d (2 => DONE)" % marker.line_mode)
    print("row_pairs = %s" % (marker.row_pairs,))
    print("col_pairs = %s" % (marker.col_pairs,))


def _get_image_size(path):
    try:
        with Image.open(path) as im:
            return im.size
    except Exception:
        return None


class MarkerApp:
    """Tkinter GUI replica of ImageLineView interaction."""

    COLORS = {
        "active": "#FFDD00",
        "locked": "#00BFFF",
        "check": "#FFFFFF",
        "bg": "#FFFFFF",
        "grid": "gray",
        "text": "#FFDD00",
    }

    def __init__(self, root, img_path):
        self.root = root
        self.img_path = img_path
        self.size_width_var = tk.StringVar(value=str(DEFAULT_SIZE_WIDTH))
        self.size_height_var = tk.StringVar(value=str(DEFAULT_SIZE_HEIGHT))
        self.screen_w_var = tk.StringVar(value="480")
        self.screen_h_var = tk.StringVar(value="480")

        self._load_image(img_path)
        self.marker = ImageMarker(self.bmp_w, self.bmp_h)

        top = tk.Frame(root, bg="black")
        top.pack(fill="x")

        tk.Button(top, text="Open Image...", command=self.choose_image).pack(side="left", padx=2, pady=2)

        tk.Label(top, text="W:", bg="black", fg="white").pack(side="left")
        w_entry = tk.Entry(top, textvariable=self.size_width_var, width=6)
        w_entry.pack(side="left")
        tk.Label(top, text="H:", bg="black", fg="white").pack(side="left")
        h_entry = tk.Entry(top, textvariable=self.size_height_var, width=6)
        h_entry.pack(side="left")
        tk.Button(top, text="Apply", command=self.apply_size).pack(side="left", padx=6)

        tk.Label(top, text="ScreenW:", bg="black", fg="white").pack(side="left")
        sw_entry = tk.Entry(top, textvariable=self.screen_w_var, width=6)
        sw_entry.pack(side="left")
        tk.Label(top, text="ScreenH:", bg="black", fg="white").pack(side="left")
        sh_entry = tk.Entry(top, textvariable=self.screen_h_var, width=6)
        sh_entry.pack(side="left")

        tk.Button(top, text="Export TLV", command=self._export_tlvs).pack(side="left", padx=6)

        self.canvas = tk.Canvas(root, width=int(self.bmp_w * self.scale),
                                height=int(self.bmp_h * self.scale) + 60, bg="black")
        self.canvas.pack()
        self.canvas.bind("<Button-1>", self.on_down)
        self.canvas.bind("<B1-Motion>", self.on_move)
        self.canvas.bind("<ButtonRelease-1>", self.on_up)
        for k in ("<Up>", "<Down>", "<Left>", "<Right>", "<Shift-Up>", "<Shift-Down>",
                  "<Shift-Left>", "<Shift-Right>"):
            self.canvas.bind(k, self.on_arrow)
        self.canvas.focus_set()

        self.status = tk.Label(root, text="Confirm 4 horizontal lines (drag the yellow line, click the green check)",
                               bg="black", fg="white")
        self.status.pack(fill="x")

        self.drag_mode = 0
        self.last = (0, 0)
        self.show_mag = False
        self.mag_x = 0.0
        self.mag_y = 0.0

    def _load_image(self, path):
        if path:
            self.img = Image.open(path)
        else:
            self.img = Image.new("RGB", (480, 347), "white")
        self.bmp_w, self.bmp_h = self.img.size
        self.scale = 1.0
        self.img_left = 0.0
        self.img_top = 0.0
        disp = self.img.resize((int(self.bmp_w * self.scale), int(self.bmp_h * self.scale)), Image.LANCZOS)
        self.photo = ImageTk.PhotoImage(disp)

    def choose_image(self):
        from tkinter import filedialog
        path = filedialog.askopenfilename(
            filetypes=[("Images", "*.png *.jpg *.jpeg *.bmp *.gif"), ("All files", "*.*")])
        if not path:
            return
        self.img_path = path
        self._load_image(path)
        self.marker = ImageMarker(self.bmp_w, self.bmp_h)
        self.loaded()
        self.redraw()

    def loaded(self):
        try:
            self.canvas.config(width=int(self.bmp_w * self.scale),
                               height=int(self.bmp_h * self.scale) + 60)
        except Exception:
            pass

    def apply_size(self):
        try:
            w = int(self.size_width_var.get())
            h = int(self.size_height_var.get())
            self.marker.set_input_size(w, h)
        except ValueError:
            self.status.config(text="Invalid W/H value")

    def on_arrow(self, event):
        if not self.has_line() or self.marker.active_line.locked:
            return
        step = 10 if event.state & 0x0001 else 1  # Shift => 10px
        line = self.marker.active_line
        if line.orientation == ORIENTATION_HORIZONTAL and event.keysym in ("Up", "Down"):
            d = -step if event.keysym == "Up" else step
            self.marker.set_active_position(line.position + d)
        elif line.orientation == ORIENTATION_VERTICAL and event.keysym in ("Left", "Right"):
            d = -step if event.keysym == "Left" else step
            self.marker.set_active_position(line.position + d)
        self.redraw()

    def s2p(self, x, y):
        return x * self.scale, y * self.scale

    def on_down(self, event):
        x, y = event.x, event.y
        self.last = (x, y)
        if self.has_line() and self._near(x, y):
            self.drag_mode = 1
            self.mag_x, self.mag_y = x, y
            self.show_mag = True
            self.try_confirm(x, y)
        else:
            self.drag_mode = 2
        self.redraw()

    def on_move(self, event):
        x, y = event.x, event.y
        if self.drag_mode == 1 and not self.marker.active_line.locked:
            dx = (x - self.last[0]) / self.scale
            dy = (y - self.last[1]) / self.scale
            self.marker.set_active_position(self.marker.active_line.position + (dy if
                    self.marker.active_line.orientation == ORIENTATION_HORIZONTAL else dx))
            self.last = (x, y)
            self.mag_x, self.mag_y = x, y
        elif self.drag_mode == 2:
            self.img_left += (x - self.last[0]) / self.scale
            self.img_top += (y - self.last[1]) / self.scale
            self.last = (x, y)
        self.redraw()

    def on_up(self, event):
        self.drag_mode = 0
        self.show_mag = False
        self.redraw()

    def has_line(self):
        return self.marker.active_line is not None

    def _near(self, x, y):
        line = self.marker.active_line
        if line.orientation == ORIENTATION_HORIZONTAL:
            pos = (self.img_top + min(max(line.position, 0), self.bmp_h)) * self.scale
            return abs(y - pos) <= LINE_TOUCH_RADIUS
        pos = (self.img_left + min(max(line.position, 0), self.bmp_w)) * self.scale
        return abs(x - pos) <= LINE_TOUCH_RADIUS

    def _check_hit(self, x, y):
        # green confirm button near the end of active line
        line = self.marker.active_line
        if line.orientation == ORIENTATION_HORIZONTAL:
            cx = (self.img_left + self.bmp_w - 35) * self.scale
            cy = (self.img_top + min(max(line.position, 0), self.bmp_h)) * self.scale
        else:
            cx = (self.img_left + min(max(line.position, 0), self.bmp_w)) * self.scale
            cy = (self.img_top + self.bmp_h - 35) * self.scale
        return abs(x - cx) <= 25 * self.scale and abs(y - cy) <= 25 * self.scale

    def try_confirm(self, x, y):
        if self.has_line() and not self.marker.active_line.locked and self._check_hit(x, y):
            self.marker.confirm_current_line()

    def redraw(self):
        c = self.canvas
        c.delete("all")
        c.create_image(int(self.img_left * self.scale), int(self.img_top * self.scale),
                       anchor="nw", image=self.photo)

        for line in self.marker.locked_lines:
            self._draw_line(line, self.COLORS["locked"])
        if self.has_line():
            self._draw_line(self.marker.active_line, self.COLORS["text"])
            if not self.marker.active_line.locked:
                self._draw_check()
        if self.show_mag and self.has_line():
            self._draw_magnifier()

        mode_txt = ("horizontal" if self.marker.line_mode == MODE_HORIZONTAL else
                    "vertical" if self.marker.line_mode == MODE_VERTICAL else "DONE")
        self.status.config(text="mode=%s  H:%d/4 V:%d/3  drag line to move, tap green to confirm" %
                          (mode_txt, self.marker.horizontal_confirmed, self.marker.vertical_confirmed))

    def _export_tlvs(self):
        """Build final TLV bytes using offset arrays + cancel, write binary file."""
        if self.marker.line_mode != MODE_DONE or self.marker.generated_key_locs is None:
            self.status.config(text="Export failed: confirm all 4 horizontal + 3 vertical lines first")
            return
        from tkinter import filedialog
        out_path = filedialog.asksaveasfilename(
            defaultextension=".tlv",
            filetypes=[("TLV", "*.tlv"), ("All files", "*.*")])
        if not out_path:
            return
        screen = [int(self.screen_w_var.get()), int(self.screen_h_var.get())]
        keys, func = get_tlvs_from_arrays(screen, self.bmp_h,
                                          self.marker.generated_key_locs,
                                          self.marker.generated_function_key_locs)
        n = export_tlvs_hex_file(self.img, [0, screen[1] - self.bmp_h, screen[0], screen[1]],
                                 keys, func, out_path)
        self.status.config(text="Exported %d bytes -> %s" % (n, out_path))

    def _draw_line(self, line, color):
        c = self.canvas
        if line.orientation == ORIENTATION_HORIZONTAL:
            y = (self.img_top + min(max(line.position, 0), self.bmp_h)) * self.scale
            c.create_line(self.img_left * self.scale, y,
                          (self.img_left + self.bmp_w) * self.scale, y, fill=color, width=2)
            c.create_text(self.img_left * self.scale + 10, y - 12, text=str(int(line.position)),
                          fill=self.COLORS["text"], anchor="w")
        else:
            x = (self.img_left + min(max(line.position, 0), self.bmp_w)) * self.scale
            c.create_line(x, self.img_top * self.scale,
                          x, (self.img_top + self.bmp_h) * self.scale, fill=color, width=2)
            c.create_text(x + 6, self.img_top * self.scale + 20, text=str(int(line.position)),
                          fill=self.COLORS["text"], anchor="w")

    def _draw_check(self):
        c = self.canvas
        line = self.marker.active_line
        if line.orientation == ORIENTATION_HORIZONTAL:
            cx = (self.img_left + self.bmp_w - 35) * self.scale
            cy = (self.img_top + min(max(line.position, 0), self.bmp_h)) * self.scale
        else:
            cx = (self.img_left + min(max(line.position, 0), self.bmp_w)) * self.scale
            cy = (self.img_top + self.bmp_h - 35) * self.scale
        r = 25 * self.scale
        c.create_oval(cx - r, cy - r, cx + r, cy + r, fill="#00FF00", outline="")
        c.create_line(cx - 7 * self.scale, cy, cx - 1 * self.scale, cy + 7 * self.scale,
                      cx + 8 * self.scale, cy - 7 * self.scale, fill="white", width=3)

    def _draw_magnifier(self):
        c = self.canvas
        line = self.marker.active_line
        zoom = MAGNIFIER_ZOOM
        size = MAGNIFIER_SIZE * self.scale

        # magnifier center (Android: centerX follows finger, centerY pinned to the line,
        # or vice versa), clamped inside the image
        if line.orientation == ORIENTATION_HORIZONTAL:
            # real image coord of the line
            line_img_y = self.img_top + min(max(line.position, 0), self.bmp_h)
            cx = min(max(self.mag_x, self.img_left * self.scale),
                     (self.img_left + self.bmp_w) * self.scale)
            cy = line_img_y * self.scale
        else:
            cx = (self.img_left + min(max(line.position, 0), self.bmp_w)) * self.scale
            cy = min(max(self.mag_y, self.img_top * self.scale),
                     (self.img_top + self.bmp_h) * self.scale)

        # position the magnifier box to the side of the finger
        left = cx + 30
        top = cy - size / 2
        if left + size > self.canvas.winfo_width():
            left = cx - 30 - size
        if top < 0:
            top = 0
        if top + size > self.canvas.winfo_height() - 60:
            top = self.canvas.winfo_height() - 60 - size

        # image-space region the magnifier shows: size/zoom screen pixels -> image px
        img_w_on_mag = size / (zoom)          # screen px shown inside the lens
        side = img_w_on_mag / self.scale      # image px
        icx = (cx - self.img_left * self.scale) / self.scale   # image px at lens center
        icy = (cy - self.img_top * self.scale) / self.scale

        sx0 = icx - side / 2
        sy0 = icy - side / 2
        cropped = self.img.crop((sx0, sy0, sx0 + side, sy0 + side)).resize(
            (int(size), int(size)), Image.LANCZOS)
        self._mag_photo = ImageTk.PhotoImage(cropped)
        c.create_image(left, top, anchor="nw", image=self._mag_photo)
        c.create_rectangle(left, top, left + size, top + size, outline="#FFDD00", width=2)

        # reference line drawn at lens center (it is the image center of the lens)
        if line.orientation == ORIENTATION_HORIZONTAL:
            c.create_line(left, top + size / 2, left + size, top + size / 2,
                          fill="#FFDD00", width=2)
        else:
            c.create_line(left + size / 2, top, left + size / 2, top + size,
                          fill="#FFDD00", width=2)

        # grid
        step = 10 * zoom * self.scale
        gx = left
        while gx <= left + size:
            c.create_line(gx, top, gx, top + size, fill="gray")
            gx += step
        gy = top
        while gy <= top + size:
            c.create_line(left, gy, left + size, gy, fill="gray")
            gy += step

        # position label
        c.create_text(left + 4, top + 14, text=str(int(line.position)),
                      fill="#FFDD00", anchor="nw")


def main(argv):
    args = [a for a in argv[1:]]
    if "--demo" in args:
        demo()
        return

    img_path = args[0] if args else None

    if not _HAS_GUI:
        print("GUI not available (need tkinter + Pillow); running demo.")
        demo()
        return

    root = tk.Tk()
    root.title("Q3MU Line Marker (%s)" % (img_path or "no image"))
    root.lift()
    root.attributes("-topmost", True)
    root.after(0, lambda: (root.attributes("-topmost", False), None))
    app = MarkerApp(root, img_path)
    if img_path is None:
        root.after(100, app.choose_image)
    app.redraw()
    root.mainloop()


if __name__ == "__main__":
    main(sys.argv)