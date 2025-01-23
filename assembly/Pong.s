# Initialization
li x1, 0b0000000000000001	# x1: Minimum position           
li x5, 0b0000000000000100   # x5: Ball's position
li x7, 0b1000000000000000	# x7: Maximum position
li x6, 1             		# x6 is the direction (0: right, 1: left)
li x8, 1024                 # x8: LED output address
li x9, 1026                 # x9: BTN input address
sw x5, 0(x8)                # Store the value of x5 to the LED output


start:
    lw x2, 1026(x0) # Get user input
    nop
    andi x2, x2, 0b0100
    beq x2, x0, start

loop:	# Write the current ball position to LEDs
    sw x5, 0(x8)          # Store the value of x5 to the LED output

    # Delay loop
    #li x28, 10000000    # x28 is used for the delay count
    li x28, 1            # delay count for testing

    delay:
        addi x28, x28, -1
        bnez x28, delay

        # Check button input
        lw x29, 0(x9)          # Load button input state into x29

        # Update ball position and check for player interaction
        beq x6, zero, move_right  # If direction is right (0), move right

move_left:
    slli x5, x5, 1         		# Shift the ball left
    li x30, 0b1110000000000000	# Mask for last three spots on the left
    and x31, x5, x30        	# Check if ball is in the leftmost three spots
    beq x31, x0, check_bounds_L	# If not in the leftmost three spots, check bounds
    andi x30, x29, 0b1000   	# Check left player button (4th bit)
    nop
    bne x30, x0, continue_game 	# If button pressed, continue game, otherwise continue to "check_bounds_L"

check_bounds_L:
    nop
    bge x5, x7, right_win		# If ball reaches the leftmost position, right player wins
    jal loop

move_right:
    srli x5, x5, 1         		# Shift the ball right
    li x30, 0b0000000000000111	# Mask for last three spots on the right
    and x31, x5, x30        	# Check if ball is in the rightmost three spots
    beq x31, x0, check_bounds_R	# If not in the rightmost three spots, check bounds
    andi x30, x29, 0b10     	# Check right player button (2nd bit)
    nop
    bne x30, x0, continue_game 	# If button pressed, continue game, otherwise continue to "check_bounds_R"

check_bounds_R:
    nop
    beq x5, x1, left_win   # If ball reaches the rightmost position, left player wins
    jal loop

left_win:
    li x5, 0b1110000000000000	# Light up the LEDs on the right side for the left player win
    sw x5, 0(x8)
    jal end

right_win:
    li x5, 0b0000000000000111	# Light up the LEDs on the left side for the right player win
    sw x5, 0(x8)
    jal end

continue_game:
    # xori x6, x6, 1        	# Toggle the direction
    jal loop

    
end: # End program (not reachable, but good practice)
    jal end
