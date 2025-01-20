addi x1, x0, 100

#Initialize values
addi x20, x0, 5
addi x21, x0, 7
addi x22, x0, 2
addi x23, x0, 6
addi x24, x0, 2
addi x25, x0, 9
addi x26, x0, -1
addi x27, x0, 3
addi x28, x0, 2

#Store in memory
sw x20, 0(x1)
sw x21, 4(x1)
sw x22, 8(x1)
sw x23, 12(x1)
sw x24, 16(x1)
sw x25, 20(x1)
sw x26, 24(x1)
sw x27, 28(x1)
sw x28, 32(x1)

#Code
lw x2, 0(x1)
addi x10, x0, 0
nop
loop:
    nop
    nop
    addi x1, x1, 4
    blt  x2, x0, done
    nop
    nop
    add  x10, x10, x2
    lw   x2, 0(x1)
    beq  x0, x0 loop
done:
    nop
    nop
    nop
    addi a7, x0, 10