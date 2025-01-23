# 5-cell neighborhood: YYXYY
# A cell is born if it has 2 or 3 Y-neighbors alive
# A living cell survives if it has 2 or 4 Y-neighbors

start:
    addi t0, x0, 0b0001 # Bit mesh for button
    addi s10, x0, 0     # Iteration counter
    lw a0, 1025(x0)     # Switches
    lw a1, 1026(x0)     # Buttons
    sw a0, 1024(x0)    # LEDs
    beq a1, t0, init_exec
    jal x0, start
init_exec:
    sw a0, 1024(x0)  # LEDs
    sw s10, 1027(x0) # Seven Segment Display
    beq a0, x0, done
    slli, a3, a0, 2
    addi a1, x0, 0  # Loop counter
    addi a2, x0, 16 # Loop limit
    addi x1, x0, 1
    addi x2, x0, 2
    addi x3, x0, 3
    addi x4, x0, 4
    addi a7, x0, 0  # Result
    
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
    
    # Calculate amount of neighbors
    add t0, s1, s2
    add t1, s3, s4
    add s5, t0, t1
    
    # Check whether alive or dead
    beq s0, x1, alive
    # If current cell is dead:
    beq s5, x2, set_alive
    beq s5, x3, set_alive
    jal x0, continue
    
alive:
    # If current cell is alive:
    beq s5, x2, set_alive
    beq s5, x4, set_alive
    jal x0, continue

set_alive:
    # Set the corresponding output bit
    sll t0, x1, a1
    or a7, a7, t0
    
continue:
    addi a1, a1, 1 # Add to counter
    srli a3, a3, 1 # Shift the entire input right
    blt a1, a2, exec_turn # Repeat if end is not reached
    
    # Load waiting period counter 
    li s11, 0xFF
    
    # Prepare
    addi s10, s10, 1 # Increment iterations
    add a0, x0, a7 # Copy result into argument
    add t0, x0, x0 # Init wait counter

wait:
    addi t0, t0, 1
    blt t0, s11, wait
    jal x0, init_exec
done:
    jal x0, done
    nop
    nop
    nop
    nop
