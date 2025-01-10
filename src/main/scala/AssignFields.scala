import chisel3._
import chisel3.util._

class InstructionFields extends Bundle{
  val opcode = UInt(7.W)
  val rs1 = UInt(5.W)
  val rs2 = UInt(5.W)
  val rd = UInt(5.W)
  val funct3 = UInt(3.W)
  val funct7 = UInt(7.W)
  val imm = SInt(32.W)
}

class AssignFields extends Module{
  val io = IO(new Bundle{
    val instruction = Input(UInt(32.W))
    val output = Output(new InstructionFields())
  })

  // Instruction Fields
  val opcode = io.instruction(6, 0)

  // Assign Default placement Fields
  io.output.opcode := opcode
  io.output.rs1 := io.instruction(19, 15)
  io.output.rs2 := io.instruction(24, 20)
  io.output.rd := io.instruction(11, 7)
  io.output.funct3 := io.instruction(14, 12)
  io.output.funct7 := io.instruction(31, 25)
  io.output.imm := 0.S


  // Instruction Types
  val R_Type = "b0110011".U     // Arithmetic/Logic
  val I_Type_1 = "b0010011".U   // Arithmetic/Logic immediate
  val I_Type_2 = "b0000011".U   // Load
  val I_Type_3 = "b1100111".U   // jalr
  val I_Type_4 = "b1110011".U   // ecall/ebreak
  val S_Type = "b0100011".U     // Store
  val B_Type = "b1100011".U     // Branch
  val J_Type = "b1101111".U     // jal
  val U_Type = "b0110111".U     // lui and auipc


  // Assign Immediate values
  switch(opcode) {
    is(R_Type) {
      // Default Case, is assigned outside of switch statement
    }

    is(I_Type_1, I_Type_2, I_Type_3, I_Type_4) {
      io.output.imm := io.instruction(31, 20).asSInt
    }

    is(S_Type) {
      io.output.imm := Cat(io.instruction(31, 25), io.instruction(11, 7)).asSInt
    }

    is(B_Type) {
      io.output.imm := Cat(io.instruction(31), io.instruction(7),
                           io.instruction(30, 25), io.instruction(11, 8), 0.U(1.W)).asSInt
    }

    is(U_Type) {
      io.output.imm := (Cat(io.instruction(31, 12), 0.U(12.W))).asSInt
    }

    is(J_Type) {
      io.output.imm := Cat(io.instruction(31), io.instruction(19, 12),
                           io.instruction(20), io.instruction(30, 21), 0.U(1.W)).asSInt
    }
  }
}
