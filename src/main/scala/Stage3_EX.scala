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

class Stage3_EX(fpga: Boolean) extends Module {
  val io = IO(new Bundle{
    val pipeline_vals = Input(new PipelineValuesEX)
    val data_out = Output(SInt(32.W))
    val rd = Output(UInt(5.W))
    val ctrl = Output(new ControlBus)
  })

  // Pipeline registers
  val pipeline_regs = Reg(new PipelineValuesEX)
  pipeline_regs := io.pipeline_vals

  // ALU
  val ALU = Module(new ALU_Control)
  ALU.io.input := pipeline_regs

  // Output
  io.data_out := ALU.io.result
  //io.check_out := ALU.io.check // For B-Type instructions, must be implemented for branching
  io.rd := pipeline_regs.rd
  io.ctrl := pipeline_regs.ctrl
}
