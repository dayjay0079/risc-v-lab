import chisel3._
import chisel3.util._
import lib.ControlBus

class BranchPrediction extends Module{
  val io = IO(new Bundle{
    val pc = Input(UInt(32.W))
    val instruction = Input(UInt(32.W))
    val pc_prediction = Output(UInt(32.W))
    val branch_taken = Output(Bool())
  })
  // Instruction fields
  val imm = WireDefault(0.S(32.W))
  val opcode = WireDefault(0.U(7.W))

  // Instruction Types
  val B_Type = "b1100011".U     // Branch
  val J_Type = "b1101111".U     // jal

  // Assign opcode
  opcode := io.instruction(6, 0)

  when(opcode === B_Type) {
    imm := Cat(io.instruction(31), io.instruction(7),
               io.instruction(30, 25), io.instruction(11, 8)).asSInt << 1
  } .elsewhen(opcode === J_Type) {
    imm := Cat(io.instruction(31), io.instruction(19, 12),
               io.instruction(20), io.instruction(30, 21)).asSInt << 1
  }

  // Branch "prediction" - currently branch is assumed taken
  when(opcode === B_Type | opcode === J_Type) {
    io.pc_prediction := (io.pc.asSInt + imm).asUInt
    io.branch_taken := true.B
  } .otherwise {
    io.pc_prediction := io.pc + 4.U
    io.branch_taken := false.B
  }
}
