import chisel3._
import chisel3.util._

private class PipelineValues extends Bundle {
  val instruction = UInt(32.W)
}

class Stage2_ID(fpga: Boolean) extends Module {
  val io = IO(new Bundle {
    val pipeline_vals = Input(new PipelineValues)
    val rd_in = Input(UInt(5.W))
    val data_in = Input(SInt(32.W))
    val write_enable = Input(Bool())
    val data_out1 = Output(SInt(32.W))
    val data_out2 = Output(SInt(32.W))
    val imm = Output(SInt(32.W))
    val rd_out = Output(UInt(5.W))
    val opcode = Output(UInt(7.W))
    val funct3 = Output(UInt(3.W))
    val funct7 = Output(UInt(7.W))
  })
  val pipeline_regs = Reg(new PipelineValues)
  pipeline_regs := io.pipeline_vals

  val decoder = new AssignFields()
  decoder.io.instruction := pipeline_regs.instruction

  // Read registers
  val reg_file = new RegisterFile(fpga)
  reg_file.io.rs1 := decoder.io.output.rs1
  reg_file.io.rs2 := decoder.io.output.rs2

  // Write to registers
  reg_file.io.rd := io.rd_in
  reg_file.io.data_in := io.data_in
  reg_file.io.write_enable := io.write_enable

  // Output
  io.data_out1 := reg_file.io.data1
  io.data_out2 := reg_file.io.data2
  io.imm := decoder.io.output.imm
  io.rd_out := decoder.io.output.rd
  io.opcode := decoder.io.output.opcode
  io.funct3 := decoder.io.output.funct3
  io.funct7 := decoder.io.output.funct7
}
