import chisel3._
import chisel3.util._

class ALU extends Module{
  val io = IO(new Bundle{
    val input = Input(new PipelineValuesEX)
    val result = Output(SInt(32.W))
    val check = Output(Bool())

  })

  //Initialize I/O
  val opcode = io.input.ctrl.opcode
  val data1 = io.input.data1
  val data2 = io.input.data2
  val imm = io.input.imm
  val funct3 = io.input.ctrl.funct3
  val instruction = io.input.ctrl.inst_type

  val result = Wire(SInt(32.W))
  val check = Wire(Bool())
  io.result := result
  io.check := check

  result := 0.S
  check := false.B

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
  val is_I_Type_shift = (funct3 === "x1".U) | (funct3 === "x5".U)

  // Choose values for calculation
  switch(opcode) {
    is(R_Type, B_Type) { var1 := data1; var2 := data2 }
    is(I_Type_1) { var1 := data1; var2 := Mux(is_I_Type_shift, (Cat(0.S(27.W), imm(4,0)).asSInt), imm) }
    is(I_Type_2, I_Type_3, S_Type) { var1 := data1; var2 := imm }
    is(J_Type, U_Type_1, U_Type_2) { var1 := imm }
  }

  // Choose arithmetic instruction type
  switch(instruction) {
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
      check := var1 === var2
    }
    is(11.U) { //BNE
      check := var1 =/= var2
    }
    is(12.U) { //BLT
      check := var1 < var2
    }
    is(13.U) { //BGE
      check := var1 >= var2
    }
    is(14.U) { //BLTU
      check := var1.asUInt < var2.asUInt
    }
    is(15.U) { //BGEU
      check := var1.asUInt >= var2.asUInt
    }
    is(16.U) {
      result := 0.S
      check := 0.B
    }
  }
}
