# 5-cell neighborhood: YYXYY
# A cell is born if it has 2 or 3 Y-neighbors alive
# A living cell survives if it has 2 or 4 Y-neighbors

start:
    addi t0, x0, 0b0001
    lw a0, 1025(x0)    # Switches
    lw a1, 1026(x0)    # Buttons
    nop
    nop
    nop
    nop
    sw a0, 1024(x0)    # LEDs
    beq a1, t0, init_exec
    nop
    nop
    nop
    nop
    jal x0, start
    nop
    nop
    nop
    nop
    
init_exec:
    sw a0, 1024(x0) # LEDs
    addi a1, x0, 2  # Counter
    addi a2, x0, 16 # Loop limit
    addi x1, x0, 1
    addi x2, x0, 2
    addi x3, x0, 3
    addi x4, x0, 4
    addi a7, x0, 0  # Result

# 1st & 2nd leftmost edgecases
    srli s1, a0, 1
    srli s2, a0, 2
    srli s3, a0, 3
    nop
    andi s0, a0, 1
    andi s1, s1, 1
    andi s2, s2, 1
    andi s3, s3, 1
    nop
    nop
    nop
    nop

    # 1st
    add t0, s1, s2
    nop
    nop
    nop
    nop
    beq s0, x1, alive1
    nop
    nop
    nop
    nop
    beq t0, x2, set_alive1
    nop
    nop
    nop
    nop
    jal x0, second_edge
    nop
    nop
    nop
    nop
alive1:
    bne t0, x2, second_edge
    nop
    nop
    nop
    nop
set_alive1:
    ori a7, a7 1

second_edge:
    # 2nd
    add t0, s0, s2
    nop
    nop
    nop
    nop
    add t0, t0, s3
    nop
    nop
    nop
    nop
    beq s1, x1, alive2
    nop
    nop
    nop
    nop
    beq t0, x2, set_alive2
    nop
    nop
    nop
    nop
    beq t0, x3, set_alive2
    nop
    nop
    nop
    nop
    jal x0, exec_turn
    nop
    nop
    nop
    nop
alive2:
    bne t0, x2, exec_turn
    nop
    nop
    nop
    nop
set_alive2:
    ori a7, a7, 0b10
    
exec_turn:
    # Isolate each bit in neighborhood
    srli s0, a3, 2
    srli s1, a3, 0
    srli s2, a3, 1
    srli s3, a3, 3
    srli s4, a3, 4
    
    andi s0, s0, 1
    andi s1, s1, 1
    andi s2, s2, 1
    andi s3, s3, 1
    andi s4, s4, 1
    nop
    nop
    nop
    nop
    
    # Calculate amount of neighbors
    add t0, s1, s2
    add t1, s3, s4
    nop
    nop
    nop
    nop
    add s5, t0, t1
    
    # Check whether alive or dead
    beq s0, x1, alive
    # If current cell is dead:
    nop
    nop
    nop
    nop
    beq s5, x2, set_alive
    nop
    nop
    nop
    nop
    beq s5, x3, set_alive
    nop
    nop
    nop
    nop
    jal x0, continue
    nop
    nop
    nop
    nop
    
alive:
    # If current cell is alive:
    beq s5, x2, set_alive
    nop
    nop
    nop
    nop
    beq s5, x4, set_alive
    nop
    nop
    nop
    nop
    jal x0, continue
    nop
    nop
    nop
    nop

set_alive:
    # Set the corresponding output bit
    sll t0, x1, a1    
    nop
    nop
    nop
    nop
    or a7, a7, t0
    
continue:
    addi a1, a1, 1 # Add to counter
    nop
    nop
    nop
    nop
    srl a3, a0, a1 # Shift the entire input right
    nop
    nop
    nop
    nop
    blt a1, a2, exec_turn # Repeat if end is not reached
    nop
    nop
    nop
    nop
    
    # Load waiting period counter 
    lui s11, 0x5FF
    nop
    nop
    nop
    nop
    addi s11, s11, 0x7FF
    
    # Prepare 
    add a0, x0, a7 # Copy result into argument
    add t0, x0, x0 # Init wait counter
    nop
    nop
    nop
    nop

wait:
    addi t0, t0, 1
    nop
    nop
    nop
    nop
    blt t0, s11, wait
    nop
    nop
    nop
    nop
    jal x0, init_exec
    nop
    nop
    nop
    nop
    
