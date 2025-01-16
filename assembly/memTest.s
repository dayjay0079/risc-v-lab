# Make the memory "dirty"
addi x10, x0 0xAA
nop
nop
nop
nop
slli x10, x10, 8
nop
nop
nop
nop
addi x10, x10 0xAA
nop
nop
nop
nop
slli x10, x10, 8
nop
nop
nop
nop
addi x10, x10 0xAA
nop
nop
nop
nop
slli x10, x10, 8
nop
nop
nop
nop
addi x10, x10 0xAA
nop    
nop
nop
nop
sw x10, 0(x0)
sw x10, 4(x0)
sw x10, 8(x0)
sw x10, 12(x0)
sw x10, 16(x0)


#Load/store to dirty memory
addi x1, x0, 1001
addi x2, x0, 122
addi x3, x0, 1003
nop
nop
nop

#Store different values
sw x1, 4(x0)
sh x3, 11(x0)
sb x2, 0(x0)
sb x2, 1(x0)
sb x2, 2(x0)
sb x2, 3(x0)

#Load stored values onto registers
lw x4, 4(x0)
lh x5, 11(x0)
lb x6, 3(x0)
nop
nop
nop
add x7, x4, x5
nop
nop
nop
nop
add a7, a7, x6

