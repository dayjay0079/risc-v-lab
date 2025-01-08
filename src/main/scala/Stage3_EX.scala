import chisel3._
import chisel3.util._

private class PipelineValues extends Bundle {
  val imm = UInt(32.W)
  val rd = SInt(32.W)
  val opcode = UInt(7.W)
  val funct3 = UInt(3.W)
  val funct7 = UInt(7.W)
}

class Stage3_EX(fpga: Boolean) extends Module {
  val io = IO(new Bundle{
    val data_in1 = Input(SInt(32.W))
    val data_in2 = Input(SInt(32.W))
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

  // TEMP addi: Simply add data1 and immediate
  io.data_out := io.data_in1 + pipeline_regs.imm

  // Output
  io.rd := pipeline_regs.rd
  io.opcode := pipeline_regs.opcode
  io.funct3 := pipeline_regs.funct3
  io.funct7 := pipeline_regs.funct7
}
