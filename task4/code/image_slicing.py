import numpy as np
from PIL import Image

def to_slice(text):
    parts = []
    for p in text.split(":"):
        if p.strip() == "":
            parts.append(None)
        else:
            parts.append(int(p))
    return slice(*parts)

input_path = input("Input image: ")
row_slice = to_slice(input("Row slice (start:stop:step): "))
col_slice = to_slice(input("Column slice (start:stop:step): "))
output_path = input("Output image (.png): ")

pixels = np.array(Image.open(input_path).convert("RGB"))
result = pixels[row_slice, col_slice]

print(f"Original size: {pixels.shape[0]} x {pixels.shape[1]}")
if result.size == 0:
    print("The slice is empty, nothing to save.")
else:
    print(f"Sliced size: {result.shape[0]} x {result.shape[1]}")
    Image.fromarray(result).save(output_path)
    print(f"Saved {output_path}")
