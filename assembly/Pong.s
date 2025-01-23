# Initialization
li x1, 1              # x1 is the minimum position (1 for 16 LEDs)
li t0, 0b100          # t0 holds the ball's position (1 << position)
li t1, 1              # t1 is the direction (0: right, 1: left)
li t2, 0b100000000000000             # t2 is the maximum position (15 for 16 LEDs)
li s0, 1024           # s0 holds the address of LED output
li s1, 1026           # s1 holds the address of button input
sw t0, 0(s0)          # Store the value of t0 to the LED output


start:
    lw x2, 1026(x0) # Get user input
    nop
    andi x2, x2, 0b0100
    beq x2, x0, start

loop:
        # Write the current ball position to LEDs
        sw t0, 0(s0)          # Store the value of t0 to the LED output

        # Delay loop
        #li t3, 10000000         # t3 is used for the delay count
        li t3, 50               # delay count for testing

    delay:
        addi t3, t3, -1
        bnez t3, delay

        # Check button input
        lw t4, 0(s1)          # Load button input state into t4

        # Update ball position and check for player interaction
        beq t1, zero, move_right  # If direction is right (0), move right

move_left:
        slli t0, t0, 1         # Shift the ball left
        li t5, 0b1110000000000000 # Mask for last three spots on the left
        and t6, t0, t5        # Check if ball is in the leftmost three spots
        beqz t6, check_bounds # If not in the leftmost three spots, check bounds
        andi t5, t4, 0b1000   # Check left player button (4th bit)
        nop
        bnez t5, continue_game # If button pressed, continue game

check_bounds2:
        beq t0, x1, left_win   # If ball reaches the rightmost position, left player wins
        j loop

move_right:
        srli t0, t0, 1         # Shift the ball right
        li t5, 0b0000000000000111 # Mask for last three spots on the right
        and t6, t0, t5        # Check if ball is in the rightmost three spots
        beqz t6, check_bounds2 # If not in the rightmost three spots, check bounds
        andi t5, t4, 0b10     # Check right player button (2nd bit)
        bnez t5, continue_game # If button pressed, continue game

check_bounds:
        bge t0, t2, right_win # If ball reaches the leftmost position, right player wins
        j loop

left_win:
        li t0, 0b1110000000000000 # Light up the LEDs on the right side for the left player win
        sw t0, 0(s0)
        j end

right_win:
        li t0, 0b0000000000000111 # Light up the LEDs on the left side for the right player win
        sw t0, 0(s0)
        j end

continue_game:
        # xori t1, t1, 1        # Toggle the direction
        j loop

        # End program (not reachable, but good practice)
end:
        j end
