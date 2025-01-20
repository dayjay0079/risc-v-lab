import chisel3._
import chisel3.util._
import lib.ControlBus

class PipelineValuesEX extends Bundle {
  val data1 = SInt(32.W)
  val data2 = SInt(32.W)
  val imm = SInt(32.W)
  val rd = UInt(5.W)
  val ctrl = new ControlBus
}

class Stage3_EX extends Module {
  val io = IO(new Bundle{
    val pipeline_vals = Input(new PipelineValuesEX)
    val data_out_alu = Output(SInt(32.W))
    val data_out_reg2 = Output(SInt(32.W))
    val rd = Output(UInt(5.W))
    val imm = Output(SInt(32.W)) //Temp
    val ctrl = Output(new ControlBus)
    val pc_update_bool = Output(Bool())
    val pc_update_val = Output(UInt(32.W))
    val data_out = Output(SInt(32.W))
  })

  // Pipeline registers
  val pipeline_regs = Reg(new PipelineValuesEX)
  pipeline_regs := io.pipeline_vals

  // ALU
  val ALU = Module(new ALU)
  ALU.io.input := pipeline_regs

  // Output
  io.data_out := ALU.io.result
  io.imm := pipeline_regs.imm // Debugging
  io.data_out_alu := ALU.io.result
  io.data_out_reg2 := pipeline_regs.data2
  io.rd := pipeline_regs.rd
  io.ctrl := pipeline_regs.ctrl
  io.pc_update_bool := ALU.io.pc_update_bool
  io.pc_update_val := ALU.io.pc_update_val
}
