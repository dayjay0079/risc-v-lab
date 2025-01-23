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
    val data_out_hazard = Output(SInt(32.W))
    val rd = Output(UInt(5.W))
    val write_enable = Output(Bool())
  })

  val data_out = Mux(io.pipeline_vals.ctrl.mem_to_reg, io.pipeline_vals.data_in_mem, io.pipeline_vals.data_in_alu)

  // Output
  io.rd := io.pipeline_vals.rd
  io.write_enable := io.pipeline_vals.ctrl.write_enable_reg

  io.data_out := data_out
  io.data_out_hazard := RegNext(data_out)
}
