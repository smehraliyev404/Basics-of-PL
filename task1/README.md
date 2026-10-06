# Task 1 | Technical Report

## What is endianness

Endianness is the order in which the bytes of a bigger value (like a 32-bit int) are stored in memory. One byte has no order problem, but as soon as a number takes 4 bytes, the computer has to decide which byte goes to the first address.

So for the number `0x12345678`, the "big end" is `12` (most significant byte) and the "little end" is `78` (least significant byte):

```
address:         +0  +1  +2  +3
big endian:      12  34  56  78
little endian:   78  56  34  12
```

Big-endian puts the most significant byte first, the same way we write numbers on paper. Little-endian puts the least significant byte first.

## Where the name comes from

While reading about it I found that the names come from Danny Cohen's note "On Holy Wars and a Plea for Peace" (1980). He took them from Gulliver's Travels, where two nations go to war over which end of an egg should be broken. Cohen compared this to the arguments of his time about which byte should be sent first over the network. In the same note he also shows that some machines were not even consistent: the PDP-11 was little-endian, but it kept 32-bit values in two registers in big-endian order.

## Who uses what

- x86 (Intel/AMD) processors are little-endian.
- ARM can work in both modes ("bi-endian"), and a bit in a system register selects the mode.
- Network protocols (IP, TCP, UDP) use big-endian, which is why it is called "network byte order".
- In Python you can choose the order yourself with `struct`: `<` for little, `>` for big, `!` for network.

## Small experiment

I wanted to see it on my own laptop (MacBook M3 Pro chip, arm64). So I wrote a small C program that prints the bytes of `0x12345678` one by one, and also converts it to network order with `htonl`:

```c
#include <stdio.h>
#include <stdint.h>
#include <arpa/inet.h>

int main(void) {
    uint32_t x = 0x12345678;
    uint32_t n = htonl(x);
    unsigned char *p = (unsigned char *)&x;
    unsigned char *q = (unsigned char *)&n;
    for (int i = 0; i < 4; i++)
        printf("addr+%d: host %02x  network %02x\n", i, p[i], q[i]);
    return 0;
}
```

Output:
```
addr+0: host 78  network 12
addr+1: host 56  network 34
addr+2: host 34  network 56
addr+3: host 12  network 78
```

So my Mac is little-endian, and network order is the reverse. And if someone takes my bytes `78 56 34 12` and reads them as big-endian, they get `0x78563412`, which is a completely different number. This is exactly the bug you get when one machine writes a binary file and another machine reads it with the other order.

## My critics

Firstly, I think the whole big vs little debate does not matter much by itself. Cohen says the same at the end of his note: agreeing on one order is more important than which order is chosen. Each side has small advantages. Little-endian lets you read a smaller part of a number from the same address. Big-endian is easier to read in a hex dump, because it looks like the number we write.

Secondly, the real problem is that we ended up with both. Most of our computers are little-endian, but the network is big-endian. So every program that sends numbers over the network has to convert them (`htonl`, `ntohl`). If you forget it, the code still compiles and works on your own machine, and only breaks when it talks to another machine. I think that is the worst kind of bug.

Thirdly, high level languages hide it, so most programmers do not even know about it until they read a binary file or a socket. I like that Python's `struct` makes you write the order (`<` or `>`) explicitly. But its default "native" order is a trap, because the same code can give different bytes on different machines.

Lastly, "big" and "little" is a bit too simple. As Cohen shows with the PDP-11, the order of bits, bytes and words can each be different. And bi-endian CPUs like ARM add one more setting to get wrong.

So my conclusion is that the byte order should be part of a file or protocol format, not something taken from the machine. When data leaves the program, the order should always be written explicitly.

## References

1. D. Cohen, "On Holy Wars and a Plea for Peace", IEN 137, 1980. https://www.rfc-editor.org/ien/ien137.txt
2. J. Reynolds, J. Postel, "Assigned Numbers", RFC 1700, 1994, section "Data Notations". https://www.rfc-editor.org/rfc/rfc1700.txt
3. Intel 64 and IA-32 Architectures Software Developer's Manual, Vol. 1, section 1.3.1 "Bit and Byte Order". https://cdrdv2-public.intel.com/671436/253665-sdm-vol-1.pdf
4. Arm, SCTLR_EL1 register (EE and E0E bits). https://developer.arm.com/documentation/ddi0595/2021-06/AArch64-Registers/SCTLR-EL1--System-Control-Register--EL1-
5. J. Postel, "Internet Protocol", RFC 791, 1981, Appendix B. https://www.rfc-editor.org/rfc/rfc791.txt
6. Python docs, `struct`. https://docs.python.org/3/library/struct.html
