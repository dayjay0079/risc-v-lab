import chisel3._
import chisel3.util._

import lib.ReadAssembly
import lib.peripherals.{MemoryMappedLeds, MemoryMappedUart, StringStreamer}
import lib.peripherals.MemoryMappedUart.UartPins
import lib.Bus

object Top extends App {
  val FPGA = true
  val MEM_SIZE = 1024
  val FREQ = 100000000
  val BAUD = 9600
  val PROGRAM_NAME = "addi5.bin"
  val PROGRAM: Seq[Int] = ReadAssembly.readBin("assembly/" + PROGRAM_NAME)
  emitVerilog(
    new Top(PROGRAM, FPGA, MEM_SIZE, FREQ, BAUD),
    Array("--target-dir", "generated")
  )
}

class Top(program: Seq[Int], fpga: Boolean, mem_size: Int, freq: Int, baud: Int) extends Module {
  val io = IO(new Bundle{
    val uart = UartPins()
    val leds = Output(UInt(16.W))
  })

  val IF = Module(new Stage1_IF(program, fpga))
  val ID = Module(new Stage2_ID(fpga))
  val EX = Module(new Stage3_EX(fpga))
  val MEM = Module(new Stage4_MEM(fpga, mem_size))
  val WB = Module(new Stage5_WB(fpga))

  // Stage 1: Instruction Fetch
  IF.io.pc_update_val := EX.io.pc_update_val
  IF.io.pc_update_bool := EX.io.pc_update_bool

  // Stage 2: Instruction Decode
  ID.io.instruction := IF.io.instruction
  ID.io.rd_in := WB.io.rd
  ID.io.data_in := WB.io.data_out
  ID.io.write_enable := WB.io.write_enable
  ID.io.pc := IF.io.pc_reg

  // Stage 3: Execute operation/Calculate address
  EX.io.pipeline_vals.data1 := ID.io.data_out1
  EX.io.pipeline_vals.data2 := ID.io.data_out2
  EX.io.pipeline_vals.imm := ID.io.imm
  EX.io.pipeline_vals.rd := ID.io.rd_out
  EX.io.pipeline_vals.ctrl := ID.io.ctrl

  // Stage 4: Memory access (if necessary)
  MEM.io.data_write := EX.io.data_out_reg2
  MEM.io.data_in := EX.io.data_out_alu
  MEM.io.rd_in := EX.io.rd
  MEM.io.ctrl_in := EX.io.ctrl

  // Stage 5: Write back (if necessary)
  WB.io.pipeline_vals.data_in_alu := MEM.io.data_out_alu
  WB.io.pipeline_vals.data_in_mem := MEM.io.data_out_mem
  WB.io.pipeline_vals.rd := MEM.io.rd_out
  WB.io.pipeline_vals.ctrl := MEM.io.ctrl_out

  // Output for testing
//  io.pc := IF.io.pc
}
