import chisel3._
import chisel3.util._

class ALU extends Module{
  val io = IO(new Bundle{
    val input1 = Input(SInt(32.W))
    val input2 = Input(SInt(32.W))
    val instruction = Input(UInt(5.W))
    val result = Output(SInt(32.W))
    val check = Output(Bool())
  })

  //Initialize I/O
  val input1 = io.input1
  val input2 = io.input2
  val instruction = io.instruction
  val result = Wire(SInt(32.W))
  val check = Wire(Bool())
  io.result := result
  io.check := check

  result := 0.S
  check := false.B

  switch(instruction) {
    is(0.U) { //ADD
      result := input1 + input2
    }
    is(1.U) { //SUB
      result := input1 - input2
    }
    is(2.U) { //XOR
      result := input1 ^ input2
    }
    is(3.U) { //OR
      result := input1 | input2
    }
    is(4.U) { //AND
      result := input1 & input2
    }
    is(5.U) { //SLL
      result := Mux(input2 >= 32.S, 0.S, input1 << input2(4, 0).asUInt)
    }
    is(6.U) { //SRL
      result := (input1.asUInt >> input2.asUInt).asSInt
    }
    is(7.U) { //SRA
      result := input1 >> input2.asUInt
    }
    is(8.U) { //SLT
      result := Mux(input1 < input2, 1.S(32.W), 0.S(32.W))
    }
    is(9.U) { //SLTU
      result := Mux(input1.asUInt < input2.asUInt, 1.S(32.W), 0.S(32.W))
    }
    is(10.U) { //BEQ
      check := input1 === input2
    }
    is(11.U) { //BNE
      check := input1 =/= input2
    }
    is(12.U) { //BLT
      check := input1 < input2
    }
    is(13.U) { //BGE
      check := input1 >= input2
    }
    is(14.U) { //BLTU
      check := input1.asUInt < input2.asUInt
    }
    is(15.U) { //BGEU
      check := input1.asUInt >= input2.asUInt
    }
    is(16.U) {
      result := 0.S
      check := 0.B
    }
  }
}
