import chisel3._
import chisel3.util._
import lib.ControlBus

class PipelineValuesWB extends Bundle {
  val data_in_alu = SInt(32.W)
  val data_in_mem = SInt(32.W)
  val rd = UInt(5.W)
  val ctrl = new ControlBus
}

class Stage5_WB extends Module {
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
  io.data_out := Mux(pipeline_regs.ctrl.mem_to_reg, pipeline_regs.data_in_mem, pipeline_regs.data_in_alu)
  io.rd := pipeline_regs.rd
  io.write_enable := pipeline_regs.ctrl.write_enable_reg
}
