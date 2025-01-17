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
    val instruction = Output(UInt(32.W))
    val pc = Output(UInt(32.W))
    val branch = Output(Bool()) //Test val
    val imm = Output(SInt(32.W))

    val EX_control = Output(UInt(4.W)) // test val
    val stall_ID = Output(Bool()) // test val
    val stall_IF = Output(Bool()) // test val
  })
  val IF = Module(new Stage1_IF(program, fpga))
  val ID = Module(new Stage2_ID(fpga))
  val EX = Module(new Stage3_EX(fpga))
  val MEM = Module(new Stage4_MEM(fpga, mem_size))
  val WB = Module(new Stage5_WB(fpga))

  // Stage 1: Instruction Fetch
  IF.io.branch_taken := ID.io.ctrl.branch_taken
  IF.io.pc_prediction := ID.io.ctrl.pc_prediction
  IF.io.pc_update_bool := EX.io.pc_update_bool
  IF.io.pc_update_val := EX.io.pc_update_val

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
  io.pc := IF.io.pc
  io.instruction := IF.io.instruction
  io.regs := ID.io.regs
  io.branch := EX.io.pc_update_bool
  io.imm := EX.io.imm

  io.EX_control := ID.io.EX_control
  io.stall_IF := ID.io.stall_IF
  io.stall_ID := ID.io.stall_ID
}
