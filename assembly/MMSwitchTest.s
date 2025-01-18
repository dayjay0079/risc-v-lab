# Load from 1025 into x1
lw x1, 1025(x0)
nop
nop
nop
nop

# Store into LEDs
sw x1, 1024(x0)
nop
nop
nop
nop