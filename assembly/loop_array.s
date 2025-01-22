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

#Reset x1
addi x1, x0, 0
#Code
addi x1, x0, 100
addi x10, x0, 0
lw x2, 0(x1)
loop:
    blt  x2, x0, done
    # This was the nops
    add  x10, x10, x2
    addi x1, x1, 4
    lw   x2, 0(x1)
    beq  x0, x0 loop
done:
    addi a7, x0, 10