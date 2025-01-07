import chisel3._
import chisel3.util._

class ALUFields extends Bundle{
  val opcode = UInt(7.W)
  val data1 = SInt(5.W)
  val data2 = SInt(5.W)
  val funct3 = UInt(3.W)
  val funct7 = UInt(7.W)
  val imm = SInt(32.W)
}

class ALU extends Module{
  val io = IO(new Bundle{
    val input = Input(new ALUFields())
    val result = Output(SInt(32.W))
    val imm = Output(SInt(32.W))
  })

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

  switch(io.input.opcode) {
    is(R_Type) {
      switch(io.input.funct3) {
        is("0x0".U) {
          switch(io.input.funct7) {
            is("0x00".U) { //ADD
              io.result := io.input.data1 + io.input.data2
            }
            is("0x20".U) { //SUB
              io.result := io.input.data1 - io.input.data2
            }
          }
        }
        is("0x4".U) {
          switch(io.input.funct7) {
            is("0x00".U) { //XOR
              io.result := io.input.data1 ^ io.input.data2
            }
          }
        }
        is("0x6".U) {
          switch(io.input.funct7) {
            is("0x00".U) { //OR
              io.result := io.input.data1 | io.input.data2
            }
          }
        }
        is("0x7".U) {
          switch(io.input.funct7) {
            is("0x00".U) { //AND
              io.result := io.input.data1 & io.input.data2
            }
          }
        }
        is("0x1".U) {
          switch(io.input.funct7) {
            is("0x00".U) { //Left-Shift
              io.result := io.input.data1 << io.input.data2
            }
          }
        }
        is("0x5".U) {
          switch(io.input.funct7) {
            is("0x00".U) { //Right-Shift
              io.result := (io.input.data1.asUInt >> io.input.data2.asUInt).asSInt
            }
            is("0x20".U) { //Right-Shift (Arithmetic)
              io.result := io.input.data1 >> io.input.data2
            }
          }
        }
        is("0x2".U) {
          switch(io.input.funct7) {
            is("0x00".U) { //Set Less Than
              io.result := Mux(io.input.data1 < io.input.data2, 1.S(32.W), 0.S(32.W))
            }
          }
        }
        is("0x3".U) {
          switch(io.input.funct7) {
            is("0x00".U) { //Set Less Than (Unsigned)
              io.result := Mux(io.input.data1.asUInt < io.input.data2.asUInt, 1.S(32.W), 0.S(32.W))
            }
          }
        }
      }
    }

    is(I_Type_1, I_Type_2, I_Type_3, I_Type_4) {
      switch(io.input.funct3) {
        is("0x0".U) { //ADD Immediate
          io.result := io.input.data1 + io.input.imm
        }
        is("0x4".U) { //XOR Immediate
          io.result := io.input.data1 ^ io.input.imm
        }
        is("0x6".U) { //OR Immediate
          io.result := io.input.data1 | io.input.imm
        }
        is("0x7".U) { //AND Immediate
          io.result := io.input.data1 & io.input.imm
        }
        is("0x1".U) { //Shift Left Logical Imm
          io.result := io.input.data1 << io.input.imm(4, 0)
          io.imm := Cat(("0x00".U(7.W)) + io.input.imm(4, 0))
        }
        is("0x5".U) {
          io.result := 0.S
        }
        is("0x2".U) {
          io.result := 0.S
        }
        is("0x3".U) {
          io.result := 0.S
        }
      }
    }

    is(S_Type) {
      switch(io.input.funct3) {

      }
    }

    is(B_Type) {
      switch(io.input.funct3) {

      }
    }

    is(U_Type) {

    }

    is(J_Type) {

    }
  }
}
