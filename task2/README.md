# Task 2 | Technical Report

The code from the task (I added the missing `print(` parts):

```python
tpl = (1, 2, 3)
print(tpl.__sizeof__())
lst = [1, 2, 3]
print(lst.__sizeof__())
```

I ran it in version 3.14.8 and the output was:

```
56
72
```

So the tuple and the list hold the same three numbers, but the list is 16 bytes bigger. I wanted to understand where these exact numbers come from, so I did some experiments and then looked at the CPython source code.

## Experiment 1: empty ones and adding elements

First I checked the empty versions: `().__sizeof__()` is 32 and `[].__sizeof__()` is 40. Then I tried different lengths with `tuple(range(n))` and `[0] * n`:

```
n:      0   1   2   3   4   5
tuple: 32  40  48  56  64  72
list:  40  48  56  64  72  80
```

Each element adds 8 bytes, which is the size of a pointer on a 64-bit machine. So the containers do not store the numbers themselves, only pointers (references) to them. To confirm this I put big values inside: `(10**100, 'a'*1000, [1]*100).__sizeof__()` is still 56, the same as `(1, 2, 3)`.

But there was something strange here. `[0] * 3` is 64, while my `[1, 2, 3]` from the task is 72. Same length, different size.

## Experiment 2: why `[1, 2, 3]` is 72

To see what Python actually does, I used `dis` to look at the bytecode:

```
LOAD_CONST   (1, 2, 3) # tpl
STORE_NAME   tpl

BUILD_LIST   0  # lst
LOAD_CONST   (1, 2, 3)
LIST_EXTEND  1
STORE_NAME   lst
```

So the tuple is just a ready constant that gets loaded. The list is created empty and then extended with the elements. Looking at the CPython source, when an empty list is extended it reserves space with `list_preallocate_exact`, and that function rounds the size up to an even number: `size = (size + 1) & ~1`. So 3 elements get space for 4, which is the extra 8 bytes. `[0] * 3` does not round, because it already knows the final size (1 element * 3) and reserves exactly 3 slots with `list_new_prealloc`. So it is 40 + 3 * 8 = 64.

## Explanation from the source code

After the experiments I looked at how CPython defines the two types.

The tuple is one block of memory, and the pointers to the elements are stored inside the object itself:

```c
typedef struct {
    PyObject_VAR_HEAD // reference count, type, size -> 3 * 8 = 24 bytes
    Py_hash_t ob_hash; // cached hash, 8 bytes
    PyObject *ob_item[1]; // the element pointers come here
} PyTupleObject;
```

So the tuple is 24 + 8 = 32 bytes of header, plus 8 bytes per element: 32 + 3 * 8 = 56. The tuple doesn't have its own `__sizeof__`, so it uses the general one from `object`, which is exactly "header size + number of elements * 8".

The list is different. The object only has a pointer to a separate array where the elements are stored, plus a field for how much space is reserved:

```c
typedef struct {
    PyObject_VAR_HEAD // 24 bytes
    PyObject **ob_item; // pointer to the element array, 8 bytes
    Py_ssize_t allocated; // reserved space, 8 bytes
} PyListObject;
```

And `list.__sizeof__` counts the reserved space (`allocated`), not the real length. So for `[1, 2, 3]`: 40 + 4 * 8 = 72.

## Conclusion

The difference comes from mutability. In the lecture slides a tuple is described as similar to a list but immutable. Because a tuple never changes, Python can store it in one block, with exactly as many slots as elements, and even reuse it as a constant. A list can grow, so it needs a pointer to a separate array, a field for the reserved size, and some extra free slots so that `append` does not need to allocate memory every time. The tuple, on the other hand, has an 8-byte hash field that the list does not have. So the list has 16 bytes of extra fields and one spare slot (8 bytes), minus the 8-byte hash, which makes it 16 bytes bigger in our example.
