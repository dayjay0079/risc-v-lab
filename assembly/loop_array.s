.data
array: .word 5
       .word 7
       .word 2
       .word 6
       .word 2
       .word 9
       .word -1
       .word 3
       .word 2
.text
    nop
    auipc x1, 0x10000
    nop
    nop
    nop
    nop
    addi x1, x1, 0
    nop
    nop
    nop
    nop
    addi x10, x0, 0
    nop
    nop
    nop
    nop
    lw x2, 0(x1)
    nop
    nop
    nop
    nop

loop:
    blt  x2, x0, done
    nop
    nop
    nop
    nop
    add  x10, x10, x2
    nop
    nop
    nop
    nop
    addi x1, x1, 4
    nop
    nop
    nop
    nop
    lw   x2, 0(x1)
    nop
    nop
    nop
    nop
    beq  x0, x0 loop
    nop
    nop
    nop
    nop
done:
    addi a7, x0, 10
    nop
    nop
    nop
    nop
