import chisel3._
import chisel3.util._

import lib.ReadAssembly
import lib.peripherals.{MemoryMappedLeds, MemoryMappedUart, StringStreamer}
import lib.peripherals.MemoryMappedUart.UartPins
import lib.Bus

object Top extends App {
  val MEM_SIZE = 1024
  val FREQ = 100000000
  val BAUD = 9600
  val LED_CNT = 16
  val PROGRAM: Seq[Int] = ReadAssembly.readBin("assembly/gol.bin")
  emitVerilog(
    new Top(PROGRAM, MEM_SIZE, FREQ, BAUD, LED_CNT),
    Array("--target-dir", "generated")
  )
}

class Top(program: Seq[Int], mem_size: Int, freq: Int, baud: Int, led_cnt: Int) extends Module {
  val io = IO(new Bundle{
    val switches = Input(UInt(16.W))
    val buttons = Input(UInt(4.W))
    val uart = UartPins()
    val leds = Output(UInt(led_cnt.W))
    val sevSeg_value = Output(UInt(8.W))
    val sevSeg_anode = Output(UInt(4.W))
  })

  val IF = Module(new Stage1_IF(program))
  val ID = Module(new Stage2_ID)
  val EX = Module(new Stage3_EX)
  val MEM = Module(new Stage4_MEM(mem_size, freq, baud, led_cnt))
  val WB = Module(new Stage5_WB)

  // Stage 1: Instruction Fetch
  IF.io.pc_update_bool := EX.io.pc_update_bool
  IF.io.pc_update_val := EX.io.pc_update_val
  IF.io.stall := ID.io.stall

  // Stage 2: Instruction Decode
  ID.io.instruction := IF.io.instruction
  ID.io.rd_in := WB.io.rd
  ID.io.data_in := WB.io.data_out
  ID.io.write_enable := WB.io.write_enable
  ID.io.pc := IF.io.pc_reg
  ID.io.branch_taken := IF.io.branch_taken
  ID.io.pc_prediction := IF.io.pc_prediction
  ID.io.flush_hazards := EX.io.flush_hazards

  // Stage 3: Execute operation/Calculate address
  EX.io.pipeline_vals.data1 := ID.io.data_out1
  EX.io.pipeline_vals.data2 := ID.io.data_out2
  EX.io.pipeline_vals.imm := ID.io.imm
  EX.io.pipeline_vals.rd := ID.io.rd_out
  EX.io.pipeline_vals.ctrl := ID.io.ctrl

  EX.io.data_in_MEM := MEM.io.data_out_forward
  EX.io.data_in_WB := WB.io.data_out_hazards
  EX.io.EX_control := ID.io.EX_control


  // Stage 4: Memory access (if necessary)
  MEM.io.switches := io.switches
  MEM.io.buttons := io.buttons
  MEM.io.data_write := EX.io.data_out_reg2
  MEM.io.data_in := EX.io.data_out_alu
  MEM.io.rd_in := EX.io.rd
  MEM.io.ctrl_in := EX.io.ctrl

  // Stage 5: Write back (if necessary)
  WB.io.pipeline_vals.data_in_alu := MEM.io.data_out_alu
  WB.io.pipeline_vals.data_in_mem := MEM.io.data_out_mem
  WB.io.pipeline_vals.rd := MEM.io.rd_out
  WB.io.pipeline_vals.ctrl := MEM.io.ctrl_out

  // Top output
  io.uart <> MEM.io.uart
  io.leds := MEM.io.leds
  io.sevSeg_value := MEM.io.sevSeg_value
  io.sevSeg_anode := MEM.io.sevSeg_anode
}
