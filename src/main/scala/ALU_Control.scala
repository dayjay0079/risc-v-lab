import chisel3._
import chisel3.util._
import lib.ControlBus

class ALUFields extends Bundle{
  val data1 = SInt(32.W)
  val data2 = SInt(32.W)
  val imm = SInt(32.W)
  val rd = UInt(5.W)
  val ctrl = new ControlBus
}

class ALU_Control extends Module{
  val io = IO(new Bundle{
    val input = Input(new PipelineValuesEX())
    val result = Output(SInt(32.W))
    val check = Output(Bool())
  })

  //Input signal placeholders
  val opcode = io.input.ctrl.opcode
  val data1 = io.input.data1
  val data2 = io.input.data2
  val funct3 = io.input.ctrl.funct3
  val funct7 = io.input.ctrl.funct7
  val imm = io.input.imm

  //Enumeration of Instructions
  object InstructionType extends ChiselEnum {
    val ADD, SUB, XOR, OR, AND, SLL, SRL, SRA, SLT, SLTU,
    BEQ, BNE, BLT, BGE, BLTU, BGEU, NaI = Value
  }
  import InstructionType._

  //ALU connection
  val ALU = Module(new ALU)
  val input1 = WireDefault(0.S(32.W))
  val input2 = WireDefault(0.S(32.W))
  val instruction = WireDefault(0.U(5.W))
  val result = ALU.io.result
  val check = ALU.io.check

  ALU.io.instruction := instruction
  ALU.io.input1 := input1
  ALU.io.input2 := input2

  //Connect to Output
  io.result := result
  io.check := check

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
  val is_I_Type_shift = (funct3 === "x1".U) | (funct3 === "x5".U)

  //Connect Input to ALU using opcodes
  switch(opcode) {
    is(R_Type, B_Type) { input1 := data1; input2 := data2 }
    is(I_Type_1) { input1 := data1; input2 := Mux(is_I_Type_shift, (Cat(0.S(27.W), imm(4,0)).asSInt), imm) }
    is(I_Type_2, I_Type_3, S_Type) { input1 := data1; input2 := imm }
    is(J_Type, U_Type_1, U_Type_2) { input1 := imm }
  }

  //Choose Instruction Type
  instruction := NaI.asUInt //Default instruction "Not an Instruction" zeroes outputs
  switch(funct3) {
    is("x0".U) {
      switch(opcode) {
        is(R_Type) { instruction := Mux(funct7_type00, ADD.asUInt, Mux(funct7_type20, SUB.asUInt, NaI.asUInt)) }
        is(I_Type_1, I_Type_2, I_Type_3, S_Type) { instruction := ADD.asUInt }
        is(B_Type) { instruction := BEQ.asUInt }
      }
    }
    is("x1".U) {
      switch(opcode) {
        is(R_Type) { instruction := Mux(funct7_type00, SLL.asUInt, NaI.asUInt) }
        is(I_Type_1) { instruction := Mux(imm_type00, SLL.asUInt, NaI.asUInt) }
        is(I_Type_2, S_Type) { instruction := ADD.asUInt }
        is(B_Type) { instruction := BNE.asUInt }
      }
    }
    is("x2".U) {
      switch(opcode) {
        is(R_Type) { instruction := Mux(funct7_type00, SLT.asUInt, NaI.asUInt) }
        is(I_Type_1) { instruction := SLT.asUInt }
        is(I_Type_2, S_Type) { instruction := ADD.asUInt }
      }
    }
    is("x3".U) {
      switch(opcode) {
        is(R_Type) { instruction := Mux(funct7_type00, SLTU.asUInt, NaI.asUInt) }
        is(I_Type_1) { instruction := SLTU.asUInt }
      }
    }
    is("x4".U) {
      switch(opcode) {
        is(R_Type) { instruction := Mux(funct7_type00, XOR.asUInt, NaI.asUInt) }
        is(I_Type_1) { instruction := XOR.asUInt }
        is(I_Type_2) { instruction := ADD.asUInt }
        is(B_Type) { instruction := BLT.asUInt }
      }
    }
    is("x5".U) {
      switch(opcode) {
        is(R_Type) { instruction := Mux(funct7_type00, SRL.asUInt, Mux(funct7_type20, SRA.asUInt, NaI.asUInt)) }
        is(I_Type_1) { instruction := Mux(imm_type00, SRL.asUInt, Mux(imm_type20, SRA.asUInt, NaI.asUInt)) }
        is(I_Type_2) { instruction := ADD.asUInt }
        is(B_Type) { instruction := BGE.asUInt }
      }
    }
    is("x6".U) {
      switch(opcode) {
        is(R_Type) { instruction := Mux(funct7_type00, OR.asUInt, NaI.asUInt) }
        is(I_Type_1) { instruction := OR.asUInt }
        is(B_Type) { instruction := BLTU.asUInt }
      }
    }
    is("x7".U) {
      switch(opcode) {
        is(R_Type) { instruction := Mux(funct7_type00, AND.asUInt, NaI.asUInt) }
        is(I_Type_1) { instruction := AND.asUInt }
        is(B_Type) { instruction := BGEU.asUInt }
      }
    }
  }
}
