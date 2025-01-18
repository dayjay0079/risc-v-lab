# 5-cell neighborhood: YYXYY
# A cell is born if it has 2 or 3 Y-neighbors alive
# A living cell survives if it has 2 or 4 Y-neighbors. (Dies if 1 or 3 neighbors)

start:
    # jump to load_input
    # 
    # jump to load input if pushed
    
load_input:
    # Load 16 binary values from the switches and store in register
    # jump to start
    
exec_turn:
    # Execute one turn, according to the rules
    # if done:
    #    jump to end
    # else:
    #    jump to store_output

store_output:
    # Write the new value to the LED address
    # Jump to wait

wait:
    # Wait for ~1 second
    # Jump to exec_turn

    
