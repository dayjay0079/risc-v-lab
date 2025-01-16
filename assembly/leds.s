loop:
lui x1, 0x5
nop
nop
nop
nop
addi x1, x1, 0x555
addi x2, x0, 0x400
nop
nop
nop
nop
sw x1, 0(x2)
lw x3, 0(x2)
jal x0, loop
nop
nop
nop
nop