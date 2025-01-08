import chisel3._
import chisel3.util._

private class PipelineValues extends Bundle {
  val data_in = SInt(32.W)
  val rd = UInt(5.W)
  val opcode = UInt(7.W)
  val funct3 = UInt(3.W)
  val funct7 = UInt(7.W)
}

class Stage4_MEM(fpga: Boolean) extends Module {
  val io = IO(new Bundle{
    val pipeline_vals = Input(new PipelineValues)
    val data_out = Output(SInt(32.W))
    val rd = Output(UInt(5.W))
    val opcode = Output(UInt(7.W))
    val funct3 = Output(UInt(3.W))
    val funct7 = Output(UInt(7.W))
  })

  // Pipeline registers
  val pipeline_regs = Reg(new PipelineValues)
  pipeline_regs := io.pipeline_vals

  // Output
  io.data_out := pipeline_regs.data_in // TEMP
  io.rd := pipeline_regs.rd
  io.opcode := pipeline_regs.opcode
  io.funct3 := pipeline_regs.funct3
  io.funct7 := pipeline_regs.funct7
}
