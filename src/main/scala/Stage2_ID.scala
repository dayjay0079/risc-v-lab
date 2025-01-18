import chisel3._
import chisel3.util._
import lib.ControlBus

class Stage2_ID(fpga: Boolean) extends Module {
  val io = IO(new Bundle {
    val instruction = Input(UInt(32.W))
    val rd_in = Input(UInt(5.W))
    val data_in = Input(SInt(32.W))
    val pc = Input(UInt(32.W))
    val write_enable = Input(Bool())

    val data_out1 = Output(SInt(32.W))
    val data_out2 = Output(SInt(32.W))
    val imm = Output(SInt(32.W))
    val rd_out = Output(UInt(5.W))
    val ctrl = Output(new ControlBus)
  })

  // Isolate instruction fields
  val control = Module(new Control())
  control.io.instruction := io.instruction

  // Hazard Module
  val hazard = Module(new Hazards())
  hazard.io.rs1 := control.io.rs1
  hazard.io.rs2 := control.io.rs2
  hazard.io.rd := control.io.rd
  hazard.io.ctrl := control.io.ctrl

  // Bundle control values
  val ctrl = Wire(new ControlBus)
  ctrl.pc := io.pc
  ctrl.opcode := control.io.ctrl.opcode
  ctrl.funct3 := control.io.ctrl.funct3
  ctrl.funct7 := control.io.ctrl.funct7
  ctrl.inst_type := control.io.ctrl.inst_type
  ctrl.write_enable_reg := control.io.ctrl.write_enable_reg
  ctrl.store_type := control.io.ctrl.store_type
  ctrl.load_type := control.io.ctrl.load_type
  ctrl.mem_to_reg := control.io.ctrl.mem_to_reg

  // Read from registers
  val reg_file = Module(new RegisterFile(fpga))
  reg_file.io.rs1 := control.io.rs1
  reg_file.io.rs2 := control.io.rs2

  // Write to registers
  reg_file.io.rd := io.rd_in
  reg_file.io.data_in := io.data_in
  reg_file.io.write_enable := io.write_enable

  // Output
  io.data_out1 := reg_file.io.data1
  io.data_out2 := reg_file.io.data2
  io.imm := RegNext(control.io.imm)
  io.rd_out := RegNext(control.io.rd)
  io.ctrl := RegNext(ctrl)
}
