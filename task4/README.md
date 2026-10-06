# Task 4 | Technical Report

For this task I wrote 2D slicing in Java and with numpy, applied both to an image, and checked whether the results are the same.

## How to run

```bash
cd task4/code
javac ImageSlicing.java
java ImageSlicing # java version
python3 image_slicing.py # numpy version
python3 compare.py # checks if two output images are the same
```

Both programs ask for the input image, the row slice, the column slice and the output file name. I wanted the slices to be written the same way as in Python, `start:stop:step`, and any part can be left empty, like `::-1` or `20:260`.

## Why an image

An image is basically a matrix of pixels: height x width, and in numpy every pixel also has 3 values (R, G, B). So `image[rows, cols]` is exactly 2D slicing, and you can see the result directly. For testing I used a photo of a cat. It is not symmetric, so the crop and the steps are easy to see:

![cat](code/images/cat.png)

## numpy version

In numpy, slicing is built in:

```python
pixels = np.array(Image.open(input_path).convert("RGB"))
result = pixels[row_slice, col_slice]
```

The only work was turning the input text into a `slice` object. I split it by `:` and empty parts become `None`, so `"::-1"` becomes `slice(None, None, -1)`.

## Java version

Java does not have slicing for arrays, so I had to write it myself. I read the image with `ImageIO` and put the pixels into an `int[][]`, where each int is one RGB color.

My first idea was simple, just loop from start to stop with the step. But then I started trying Python cases and saw that Python does more than that:

- negative indices count from the end (`-1` is the last row)
- if start or stop is outside the image, Python does not give an error, it just clips it
- when the step is negative, the default start is the last element and the default stop is "before the first one"

To get the same results as numpy I looked at how CPython does it, in `PySlice_AdjustIndices` in `Objects/sliceobject.c`, and wrote the same rules in my `fixIndex` method:

```java
static int fixIndex(int index, int length, int step) {
    if (index < 0) {
        index += length;
        if (index < 0) {
            index = (step > 0) ? 0 : -1;
        }
    } else if (index >= length) {
        index = (step > 0) ? length : length - 1;
    }
    return index;
}
```

Then `sliceIndices` makes a list of all the selected indices, and `slice` copies the selected pixels into a new matrix:

```java
for (int i = 0; i < rows.length; i++) {
    for (int j = 0; j < cols.length; j++) {
        result[i][j] = matrix[rows[i]][cols[j]];
    }
}
```

A step of 0 is not allowed, so I throw an exception.

## Results

I ran both programs with the same slices and then used `compare.py`, which loads both output images and checks with `np.array_equal` that every pixel is equal. Both were the same.

**1. Crop around the face: `[20:260, 170:390]`, size 240 x 220**

| Java | numpy |
|---|---|
| ![](code/images/java_1.png) | ![](code/images/numpy_1.png) |

**2. Every 4th pixel: `[::4, ::4]`, size 138 x 138**

| Java | numpy |
|---|---|
| ![](code/images/java_2.png) | ![](code/images/numpy_2.png) |


## Difference between the two

The results are the same, but there is one difference in how they work. In numpy, slicing does not copy anything. It returns a "view" that points to the same memory, just with a different start and step. That is also how the lecture describes a slice: "nothing more than a referencing mechanism". My Java version copies the selected pixels into a new array. It is easier to write that way, but it takes extra memory and time. It also means changing the result does not change the original image, while in numpy it would.

Also, numpy does all of this in one line. In Java I needed around 60 lines just for the slicing logic, because the language does not have array operations like slicing.

## ChatGPT

Link to the conversation: https://chatgpt.com/share/6ac52b4b-d1d4-83ed-991f-5c261537ff37

First I pasted the task text. ChatGPT answered with a generated picture instead of code, so I asked it to give me only the code that makes the sliced images and the comparison, in Java.

Then it gave a Python and a Java version. Both create a 5x5 matrix with the numbers 1 to 25, take the slice `[1:3, 1:4]` (result `[[7, 8, 9], [12, 13, 14]]`) and draw the matrices as a picture: numpy with matplotlib, Java with `Graphics2D`. In Java the slice is two loops, `for (int i = 1; i < 3; i++)` and `for (int j = 1; j < 4; j++)`.

But it is quite far from what the task wants:

- Everything is hard-coded: the matrix, the size 5 and the slice `1:3, 1:4`. In Java the loops only work for this one slice.
- There is no step and no negative indices, so it is not really numpy slicing, only a crop.
- "Graphical image" became a drawing of the numbers, not slicing a real image. The two pictures are drawn differently, so you can't compare them pixel by pixel.
- The "compare" part is only a table saying both results are the same. Nothing checks it.

I wanted to ask it to read the image and the slices from user input, but the chat stopped because I reached the limit for chats with images, so I couldn't continue.

