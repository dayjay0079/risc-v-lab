addi x1, x0, 8
addi x2, x0, 3
addi x3, x0, -5
nop
nop
nop
nop

sw x1, 0(x1)
sh x3, 5(x1)
sb x2, 0(x2)

lw x4, 0(x1)
lh x5, 5(x1)
lb x6, 0(x2)

nop
nop
nop
nop

add x3, x2, x1