import chisel3._
import chisel3.util._
import lib.ControlBus

class PipelineValuesEX extends Bundle {
  val data_in1 = SInt(32.W)
  val data_in2 = SInt(32.W)
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

  // Output
  io.data_out := pipeline_regs.data_in1 + pipeline_regs.imm // TEMP addi
  io.rd := pipeline_regs.rd
  io.ctrl := pipeline_regs.ctrl
}
