import numpy as np
from PIL import Image

java_path = input("Java output image: ")
numpy_path = input("numpy output image: ")

java_result = np.array(Image.open(java_path).convert("RGB"))
numpy_result = np.array(Image.open(numpy_path).convert("RGB"))

print(f"Java size: {java_result.shape}")
print(f"numpy size: {numpy_result.shape}")
if java_result.shape == numpy_result.shape and np.array_equal(java_result, numpy_result):
    print("Same image: every pixel is equal")
else:
    print("Different images")
