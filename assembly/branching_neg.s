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
loop:
    blt  x2, x0, done
    add  x10, x10, x2
    addi x1, x1, 4
    lw   x2, 0(x1)
    beq  x0, x0 loop
done:
    addi a7, x0, 10
    ecall
