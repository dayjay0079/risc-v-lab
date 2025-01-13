nop
addi x1, x1, 1
nop
nop
nop
nop

addi x2, x0, 1
nop
nop
nop
nop

beq x2, x1, jump
nop
nop
nop
nop
jump:
nop
nop
nop
nop

addi x1, x1, 1
nop
nop
nop
nop

addi x2, x0, 1
nop
nop
nop
nop

jump2:
beq x2, x1, jump2
nop
nop
nop
nop


