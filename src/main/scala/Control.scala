import chisel3._
import chisel3.util._

class Control extends Module{
  val io = IO(new Bundle{
    val instruction = Input(UInt(32.W))
    val opcode = Output(UInt(7.W))
    val rs1 = Output(UInt(5.W))
    val rs2 = Output(UInt(5.W))
    val rd = Output(UInt(5.W))
    val funct3 = Output(UInt(3.W))
    val funct7 = Output(UInt(7.W))
    val imm = Output(SInt(32.W))
    val inst_type = Output(UInt(5.W))
  })

  //I/O handling and internal variables
  val opcode = io.instruction(6, 0)
  val funct3 = io.instruction(14, 12)
  val funct7 = io.instruction(31, 25)
  val imm = WireDefault(0.S(32.W))
  val inst_type = WireDefault(0.U(5.W))

  io.opcode := opcode
  io.rs1 := io.instruction(19, 15)
  io.rs2 := io.instruction(24, 20)
  io.rd := io.instruction(11, 7)
  io.funct3 := funct3
  io.funct7 := funct7
  io.imm := imm
  io.inst_type := inst_type

  //Enumeration of Instruction Types
  object InstructionType extends ChiselEnum {
    val ADD, SUB, XOR, OR, AND, SLL, SRL, SRA, SLT, SLTU,
    BEQ, BNE, BLT, BGE, BLTU, BGEU, NaI = Value
  }
  import InstructionType._

  // Instruction Types
  val R_Type = "b0110011".U     // Arithmetic/Logic
  val I_Type_1 = "b0010011".U   // Arithmetic/Logic immediate
  val I_Type_2 = "b0000011".U   // Load
  val I_Type_3 = "b1100111".U   // jalr
  val S_Type = "b0100011".U     // Store
  val B_Type = "b1100011".U     // Branch
  val J_Type = "b1101111".U     // jal
  val U_Type_1 = "b0110111".U   // lui
  val U_Type_2 = "b0010111".U   // auipc

  //Bool statements for simplification
  val funct7_type00 = funct7 === "x00".U
  val funct7_type20 = funct7 === "x20".U
  val imm_type00 = imm(11,5) === "x00".U
  val imm_type20 = imm(11,5) === "x20".U

  //Assign immediate values depending on instruction type
  switch(opcode) {
    is(R_Type) {
      // Default Case, is assigned outside of switch statement
    }

    is(I_Type_1, I_Type_2, I_Type_3) {
      imm := io.instruction(31, 20).asSInt
    }

    is(S_Type) {
      imm := Cat(io.instruction(31, 25), io.instruction(11, 7)).asSInt
    }

    is(B_Type) {
      imm := Cat(io.instruction(31), io.instruction(7),
                 io.instruction(30, 25), io.instruction(11, 8), 0.U(1.W)).asSInt
    }

    is(U_Type_1, U_Type_2) {
      imm := (Cat(io.instruction(31, 12), 0.U(12.W))).asSInt
    }

    is(J_Type) {
      imm := Cat(io.instruction(31), io.instruction(19, 12),
                 io.instruction(20), io.instruction(30, 21), 0.U(1.W)).asSInt
    }
  }

  //Choose Instruction Type
  inst_type := NaI.asUInt //Default instruction "Not an Instruction" zeroes outputs
  switch(funct3) {
    is("x0".U) {
      switch(opcode) {
        is(R_Type) { inst_type := Mux(funct7_type00, ADD.asUInt, Mux(funct7_type20, SUB.asUInt, NaI.asUInt)) }
        is(I_Type_1, I_Type_2, I_Type_3, S_Type) { inst_type := ADD.asUInt }
        is(B_Type) { inst_type := BEQ.asUInt }
      }
    }
    is("x1".U) {
      switch(opcode) {
        is(R_Type) { inst_type := Mux(funct7_type00, SLL.asUInt, NaI.asUInt) }
        is(I_Type_1) { inst_type := Mux(imm_type00, SLL.asUInt, NaI.asUInt) }
        is(I_Type_2, S_Type) { inst_type := ADD.asUInt }
        is(B_Type) { inst_type := BNE.asUInt }
      }
    }
    is("x2".U) {
      switch(opcode) {
        is(R_Type) { inst_type := Mux(funct7_type00, SLT.asUInt, NaI.asUInt) }
        is(I_Type_1) { inst_type := SLT.asUInt }
        is(I_Type_2, S_Type) { inst_type := ADD.asUInt }
      }
    }
    is("x3".U) {
      switch(opcode) {
        is(R_Type) { inst_type := Mux(funct7_type00, SLTU.asUInt, NaI.asUInt) }
        is(I_Type_1) { inst_type := SLTU.asUInt }
      }
    }
    is("x4".U) {
      switch(opcode) {
        is(R_Type) { inst_type := Mux(funct7_type00, XOR.asUInt, NaI.asUInt) }
        is(I_Type_1) { inst_type := XOR.asUInt }
        is(I_Type_2) { inst_type := ADD.asUInt }
        is(B_Type) { inst_type := BLT.asUInt }
      }
    }
    is("x5".U) {
      switch(opcode) {
        is(R_Type) { inst_type := Mux(funct7_type00, SRL.asUInt, Mux(funct7_type20, SRA.asUInt, NaI.asUInt)) }
        is(I_Type_1) { inst_type := Mux(imm_type00, SRL.asUInt, Mux(imm_type20, SRA.asUInt, NaI.asUInt)) }
        is(I_Type_2) { inst_type := ADD.asUInt }
        is(B_Type) { inst_type := BGE.asUInt }
      }
    }
    is("x6".U) {
      switch(opcode) {
        is(R_Type) { inst_type := Mux(funct7_type00, OR.asUInt, NaI.asUInt) }
        is(I_Type_1) { inst_type := OR.asUInt }
        is(B_Type) { inst_type := BLTU.asUInt }
      }
    }
    is("x7".U) {
      switch(opcode) {
        is(R_Type) { inst_type := Mux(funct7_type00, AND.asUInt, NaI.asUInt) }
        is(I_Type_1) { inst_type := AND.asUInt }
        is(B_Type) { inst_type := BGEU.asUInt }
      }
    }
  }
}
