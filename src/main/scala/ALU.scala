import chisel3._
import chisel3.util._

class ALU extends Module{
  val io = IO(new Bundle{
    val input = Input(new PipelineValuesEX)
    val pc_update_val = Output(UInt(32.W))
    val pc_update_bool = Output(Bool())
    val result = Output(SInt(32.W))
  })

  //Initialize I/O
  val pc = io.input.ctrl.pc
  val opcode = io.input.ctrl.opcode
  val funct3 = io.input.ctrl.funct3
  val inst_type = io.input.ctrl.inst_type
  val imm = io.input.imm
  val data1 = io.input.data1
  val data2 = io.input.data2

  val pc_update_val = Wire(UInt(32.W))
  val pc_update_bool = Wire(Bool())
  val result = Wire(SInt(32.W))

  io.pc_update_val := pc_update_val
  io.pc_update_bool := pc_update_bool
  io.result := result

  pc_update_val := DontCare
  pc_update_bool := 0.B
  result := 0.S


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

  // Private variables
  val var1 = WireDefault(0.S(32.W))
  val var2 = WireDefault(0.S(32.W))
  val I_shift = (funct3 === "x1".U) | (funct3 === "x5".U)

  // Choose values for calculation
  switch(opcode) {
    is(R_Type) {
      var1 := data1
      var2 := data2
    }
    is(I_Type_1) {
      var1 := data1
      var2 := Mux(I_shift, (Cat(0.S(27.W), imm(4,0)).asSInt), imm)
    }
    is(I_Type_2, S_Type) {
      var1 := data1
      var2 := imm
    }
    is(B_Type) {
      var1 := data1
      var2 := data2
      pc_update_val := (pc.asSInt + imm).asUInt
    }
    is(J_Type) {
      var1 := pc.asSInt
      pc_update_val := (pc.asSInt + imm).asUInt
    }
    is(I_Type_3) {
      var1 := pc.asSInt
      pc_update_val := (data1 + imm).asUInt
    }
    is(U_Type_1, U_Type_2) {
      var1 := imm
    }
  }

  // Choose arithmetic instruction type
  switch(inst_type) {
    is(0.U) { //ADD
      result := var1 + var2
    }
    is(1.U) { //SUB
      result := var1 - var2
    }
    is(2.U) { //XOR
      result := var1 ^ var2
    }
    is(3.U) { //OR
      result := var1 | var2
    }
    is(4.U) { //AND
      result := var1 & var2
    }
    is(5.U) { //SLL
      result := Mux(var2 >= 32.S, 0.S, var1 << var2(4, 0).asUInt)
    }
    is(6.U) { //SRL
      result := (var1.asUInt >> var2.asUInt).asSInt
    }
    is(7.U) { //SRA
      result := var1 >> var2.asUInt
    }
    is(8.U) { //SLT
      result := Mux(var1 < var2, 1.S(32.W), 0.S(32.W))
    }
    is(9.U) { //SLTU
      result := Mux(var1.asUInt < var2.asUInt, 1.S(32.W), 0.S(32.W))
    }
    is(10.U) { //BEQ
      pc_update_bool := var1 === var2
    }
    is(11.U) { //BNE
      pc_update_bool := var1 =/= var2
    }
    is(12.U) { //BLT
      pc_update_bool := var1 < var2
    }
    is(13.U) { //BGE
      pc_update_bool := var1 >= var2
    }
    is(14.U) { //BLTU
      pc_update_bool := var1.asUInt < var2.asUInt
    }
    is(15.U) { //BGEU
      pc_update_bool := var1.asUInt >= var2.asUInt
    }
    is(16.U) { //JAL
      pc_update_bool := 1.B
      result := pc.asSInt + 4.S
    }
    is(17.U) { //LUI
      result := (imm(19, 0) << 12.U).asSInt
    }
    is(18.U) { //AUIPC
      result := (pc + (imm(19,0) << 12.U).asUInt).asSInt
    }
    is(19.U) { //NaI
      pc_update_val := DontCare
      pc_update_bool := 0.B
      result := 0.S
    }
  }
}
