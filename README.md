# **02114 - Design of a RISC-V Microprocessor**
## O$\pi\Omega$ - *The highly addictive processor*

### Implementation on FPGA
After writing and assembling the desired program, the path to the `.bin` file has to be set in `/src/main/scala/Top.scala` on line 14.

The project can now be compiled using by executing `sbt run`. 
The resulting verilog file located at `/generated/Top.V` is then imported into Xilinx Vivado.
In the constraints file for your desired FPGA board, the following constraints have to be set (the `.xdc` file used during development can be found in the project root):
* `clock`
* `reset`
* `io_switches[15:0]`
* `io_buttons[3:0]`
* `io_leds[15:0]`
* `io_sevSeg_value[7:0]`
* `io_sevSeg_anode[3:0]`
* `io_uart_rx`
* `io_uart_tx`

The circuit should now be able to be synthesized and implemented, from where the bitstream can be generated and loaded onto the desired FPGA board.

### Running tests
To run the waveform analysis tests we developed, simply run `sbt test`, after which the directories containing the `.vcd` waveforms will appear in `/test_run_dir/`. 

The peek/poke tests are not stored on this branch of the git-repository due to technical reasons, but can be found on the `master` branch.