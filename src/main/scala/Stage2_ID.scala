import chisel3._
import chisel3.util._
import lib.ControlBus

class Stage2_ID(fpga: Boolean) extends Module {
  val io = IO(new Bundle {
    val instruction = Input(UInt(32.W))
    val rd_in = Input(UInt(5.W))
    val data_in = Input(SInt(32.W))
    val write_enable = Input(Bool())
    val data_out1 = Output(SInt(32.W))
    val data_out2 = Output(SInt(32.W))
    val imm = Output(SInt(32.W))
    val rd_out = Output(UInt(5.W))
    val ctrl = Output(new ControlBus)
    val regs = Output(Vec(32, SInt(32.W)))
  })

  // Isolate instruction fields
  val decoder = Module(new AssignFields())
  decoder.io.instruction := io.instruction

  // Bundle control values
  val ctrl = Wire(new ControlBus)
  ctrl.opcode := decoder.io.output.opcode
  ctrl.funct3 := decoder.io.output.funct3
  ctrl.funct7 := decoder.io.output.funct7

  // Read from registers
  val reg_file = Module(new RegisterFile(fpga))
  reg_file.io.rs1 := decoder.io.output.rs1
  reg_file.io.rs2 := decoder.io.output.rs2

  // Write to registers
  reg_file.io.rd := io.rd_in
  reg_file.io.data_in := io.data_in
  reg_file.io.write_enable := io.write_enable

  // Output
  io.data_out1 := reg_file.io.data1
  io.data_out2 := reg_file.io.data2
  io.imm := (decoder.io.output.imm)
  io.rd_out := (decoder.io.output.rd)
  io.ctrl := (ctrl)
  io.regs := reg_file.io.regs
}
