addi x3, x0, 10
add x4, x3, x3
addi x5, x0, 20
add x6, x4, x3
sub x7, x4, x6
addi x7, x6, 5
add x7, x7, x3
sw x4, 0(x7)
lw x8, 0(x7) #needs to initiate stall
add x9, x8, x2
addi x3, x0, 10
add x4, x3, x3
addi x5, x0, 20
beq x9, x0, branch_target #Should initiate double stall 
# if not clashing with immediate branch taken (prediciton from mads)
add x10, x3, x4
branch_target:
    add x10, x3, x4
    addi x3, x0, 10
    add x4, x3, x3
    addi x5, x0, 20
    add x6, x4, x3
    sub x7, x4, x6
    addi x7, x6, 5
