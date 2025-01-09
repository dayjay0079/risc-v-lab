import chisel3._
import chisel3.util._
import lib.ControlBus

private class PipelineValuesMEM extends Bundle {
  val data_in = SInt(32.W)
  val rd = UInt(5.W)
  val ctrl = new ControlBus
}

class Stage4_MEM(fpga: Boolean, mem_size: Int) extends Module {
  val io = IO(new Bundle{
    val pipeline_vals = Input(new PipelineValuesMEM)
    val data_out = Output(SInt(32.W))
    val rd = Output(UInt(5.W))
    val ctrl = Output(new ControlBus)
  })

  // Pipeline registers
  val pipeline_regs = Reg(new PipelineValuesMEM)
  pipeline_regs := io.pipeline_vals

  // Output
  io.data_out := pipeline_regs.data_in // TEMP
  io.rd := pipeline_regs.rd
  io.ctrl := pipeline_regs.ctrl
}
