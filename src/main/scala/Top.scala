import chisel3._
import chisel3.util._

import lib.ReadAssembly
import lib.peripherals.{MemoryMappedUart, StringStreamer}
import lib.peripherals.MemoryMappedUart.UartPins

object Top extends App {
  val FPGA = false
  val MEM_SIZE = 1024
  val FREQ = 50000000
  val BAUD = 9600
  val PROGRAM: Seq[Int] = ReadAssembly.readBin("assembly/addi5.bin")
  emitVerilog(
    new Top(PROGRAM, FPGA, MEM_SIZE, FREQ, BAUD),
    Array("--target-dir", "generated")
  )
}

class Top(program: Seq[Int], fpga: Boolean, mem_size: Int, freq: Int, baud: Int) extends Module {
  val io = IO(new Bundle{
    val regs = Output(Vec(32, SInt(32.W)))
    val pc = Output(UInt(32.W))
  })
  val IF = Module(new Stage1_IF(program, fpga))
  val ID = Module(new Stage2_ID(fpga))
  val EX = Module(new Stage3_EX(fpga))
  val MEM = Module(new Stage4_MEM(fpga, mem_size))
  val WB = Module(new Stage5_WB(fpga))

  // Stage 1: Instruction Fetch
  IF.io.jump := false.B
  IF.io.jump_offset := 0.S
  IF.io.branch_pc := EX.io.branch_pc
  IF.io.branch_enable := EX.io.branch_enable

  // Stage 2: Instruction Decode
  ID.io.instruction := IF.io.instruction
  ID.io.rd_in := WB.io.rd
  ID.io.data_in := WB.io.data_out
  ID.io.pc := IF.io.pc_reg

  // Stage 3: Execute operation/Calculate address
  EX.io.pipeline_vals.data1 := ID.io.data_out1
  EX.io.pipeline_vals.data2 := ID.io.data_out2
  EX.io.pipeline_vals.imm := ID.io.imm
  EX.io.pipeline_vals.rd := ID.io.rd_out
  EX.io.pipeline_vals.ctrl := ID.io.ctrl

  // Stage 4: Memory access (if necessary)
  MEM.io.pipeline_vals.data_in := EX.io.data_out
  MEM.io.pipeline_vals.rd := EX.io.rd
  MEM.io.pipeline_vals.ctrl := EX.io.ctrl

  // Stage 5: Write back (if necessary)
  WB.io.pipeline_vals.data_in := MEM.io.data_out
  WB.io.pipeline_vals.rd := MEM.io.rd
  WB.io.pipeline_vals.ctrl := MEM.io.ctrl

  // Output for testing
  io.pc := IF.io.pc
  io.regs := ID.io.regs
}
