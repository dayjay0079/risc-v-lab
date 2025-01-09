import chisel3._
import chisel3.util._
import lib.ControlBus

class PipelineValuesWB extends Bundle {
  val data_in = SInt(32.W)
  val rd = UInt(5.W)
  val ctrl = new ControlBus
}

class Stage5_WB(fpga: Boolean) extends Module {
  val io = IO(new Bundle{
    val pipeline_vals = Input(new PipelineValuesWB)
    val data_out = Output(SInt(32.W))
    val rd = Output(UInt(5.W))
    val write_enable = Output(Bool())
  })

  // Pipeline registers
  val pipeline_regs = Reg(new PipelineValuesWB)
  pipeline_regs := io.pipeline_vals

  // Output
  io.data_out := pipeline_regs.data_in // TEMP
  io.rd := pipeline_regs.rd
  io.write_enable := pipeline_regs.ctrl.opcode =/= 0.U // TEMP
}
