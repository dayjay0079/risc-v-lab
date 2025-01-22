import chisel3._
import chisel3.util._
import lib.ControlBus

class Stage2_ID extends Module {
  val io = IO(new Bundle {
    val instruction = Input(UInt(32.W))
    val rd_in = Input(UInt(5.W))
    val data_in = Input(SInt(32.W))
    val pc = Input(UInt(32.W))
    val write_enable = Input(Bool())
    val branch_taken = Input(Bool())
    val pc_prediction = Input(UInt(32.W))
    val flush_hazards = Input(Bool())

    val data_out1 = Output(SInt(32.W))
    val data_out2 = Output(SInt(32.W))
    val imm = Output(SInt(32.W))
    val rd_out = Output(UInt(5.W))
    val ctrl = Output(new ControlBus)
    val EX_control = Output(UInt(4.W))
    val stall = Output(Bool())
  })

  val rd = Wire(UInt(5.W))
  val rs1 = Wire(UInt(5.W))
  val rs2 = Wire(UInt(5.W))
  val imm = Wire(SInt(32.W))

  // Stalling logic for input to control module
  val stall = WireDefault(0.B)
  val pc = io.pc
  val instruction = io.instruction
  val branch_taken = io.branch_taken
  val pc_prediction = io.pc_prediction

  val stall_reg = RegInit(false.B)
  val pc_reg = RegInit(0.U(32.W))
  val instruction_reg = RegInit(0.U(32.W))
  val branch_taken_reg = RegInit(false.B)
  val pc_prediction_reg = RegInit(0.U(32.W))

  stall_reg := stall
  pc_reg := Mux(stall_reg, pc_reg, pc)
  instruction_reg := Mux(stall_reg, instruction_reg, instruction)
  branch_taken_reg := Mux(stall_reg, branch_taken_reg, branch_taken)
  pc_prediction_reg := Mux(stall_reg, pc_prediction_reg, pc_prediction)

  // Isolate instruction fields
  val control = Module(new Control())
  control.io.pc := Mux(stall_reg, pc_reg, pc)
  control.io.instruction := Mux(stall_reg, instruction_reg, instruction)
  control.io.branch_taken := Mux(stall_reg, branch_taken_reg, branch_taken)
  control.io.pc_prediction := Mux(stall_reg, pc_prediction_reg, pc_prediction)

  // Hazard Module
  val hazard = Module(new Hazards())
  hazard.io.rs1 := control.io.rs1
  hazard.io.rs2 := control.io.rs2
  hazard.io.rd := control.io.rd
  hazard.io.ctrl := control.io.ctrl
  hazard.io.flush_hazards := io.flush_hazards
  stall := hazard.io.stall

  // Bundle control values
  val ctrl = Wire(new ControlBus)
  when(stall) {
    rd := 0.U
    rs1 := 0.U
    rs2 := 0.U
    imm := 0.S
    ctrl := hazard.io.ctrl_nop
  } .otherwise {
    rd := control.io.rd
    rs1 := control.io.rs1
    rs2 := control.io.rs2
    imm := control.io.imm
    ctrl := control.io.ctrl
  }

  // Read from registers
  val reg_file = Module(new RegisterFile)
  reg_file.io.rs1 := rs1
  reg_file.io.rs2 := rs2

  // Write to registers
  reg_file.io.rd := io.rd_in
  reg_file.io.data_in := io.data_in
  reg_file.io.write_enable := io.write_enable

  // Output
  io.data_out1 := reg_file.io.data1
  io.data_out2 := reg_file.io.data2
  io.imm := RegNext(imm)
  io.rd_out := RegNext(rd)
  io.ctrl := RegNext(ctrl)
  io.stall := RegNext(stall)
  io.EX_control := RegNext(hazard.io.EX_control)
}
