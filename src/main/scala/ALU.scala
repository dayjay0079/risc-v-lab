import chisel3._
import chisel3.util._

class ALUFields extends Bundle{
  val opcode = UInt(7.W)
  val data1 = SInt(32.W)
  val data2 = SInt(32.W)
  val funct3 = UInt(3.W)
  val funct7 = UInt(7.W)
  val imm = SInt(32.W)
}

class ALU extends Module{
  val io = IO(new Bundle{
    val input = Input(new ALUFields())
    val result = Output(SInt(32.W))
    val check = Output(Bool())
    val imm = Output(SInt(32.W))
  })

  // Initialize outputs
  io.result := 0.S
  io.check := 0.B
  io.imm := 0.S

  // Error Code
  val ERROR = -999999.S

  // Instruction Types
  val R_Type = "b0110011".U     // Arithmetic/Logic
  val I_Type_1 = "b0010011".U   // Arithmetic/Logic immediate
  val I_Type_2 = "b0000011".U   // Load
  val I_Type_3 = "b1100111".U   // jalr
  val I_Type_4 = "b1110011".U   // ecall/ebreak
  val S_Type = "b0100011".U     // Store
  val B_Type = "b1100011".U     // Branch
  val J_Type = "b1101111".U     // jal
  val U_Type_1 = "b0110111".U   // lui
  val U_Type_2 = "b0010111".U   // auipc


  // Predefine values for readability
  val funct7_type00 = io.input.funct7 === "x00".U
  val funct7_type20 = io.input.funct7 === "x20".U
  val a = WireDefault(0.S(32.W))
  val b = WireDefault(0.S(32.W))

  switch(io.input.opcode) {
    is(R_Type) {
      val ADD_SUB = "x0".U
      val XOR = "x4".U
      val OR = "x6".U
      val AND = "x7".U
      val SHIFT_LEFT = "x1".U
      val SHIFT_RIGHT = "x5".U
      val SLT = "x2".U
      val SLTU = "x3".U

      // Assign input values
      a := io.input.data1
      b := io.input.data2

      // Perform calculations
      switch(io.input.funct3) {
        is(ADD_SUB) {
          when(funct7_type00) { // ADD
            io.result := a + b
          } .elsewhen(funct7_type20) { // SUB
            io.result := a - b
          } .otherwise {
            io.result := ERROR
          }
        }
        is(XOR) {
          when(funct7_type00) { //XOR
            io.result := a ^ b
          } .otherwise {
            io.result := ERROR
          }
        }
        is(OR) {
          when(funct7_type00) {
            io.result := a | b
          } .otherwise {
            io.result := ERROR
          }
        }
        is(AND) {
          when(funct7_type00) {
            io.result := a & b
          } .otherwise {
            io.result := ERROR
          }
        }
        is(SHIFT_LEFT) {
          when(funct7_type00) {
            io.result := Mux(b >= 32.S, 0.S, a << b(4,0).asUInt)
          } .otherwise {
            io.result := ERROR
          }
        }
        is(SHIFT_RIGHT) {
          when(funct7_type00) {
            io.result := (a.asUInt >> b.asUInt).asSInt
          } .elsewhen(funct7_type20) {
            io.result := a >> b.asUInt
          } .otherwise {
            io.result := ERROR
          }
        }
        is(SLT) {
          when(funct7_type00) {
            io.result := Mux(a < b, 1.S(32.W), 0.S(32.W))
          } .otherwise {
            io.result := ERROR
          }
        }
        is(SLTU) {
          when(funct7_type00) {
            io.result := Mux(a.asUInt < b.asUInt, 1.S(32.W), 0.S(32.W))
          } .otherwise {
            io.result := ERROR
          }
        }
      }
    }

    is(I_Type_1) {
      val ADD_SUB = "x0".U
      val XOR = "x4".U
      val OR = "x6".U
      val AND = "x7".U
      val SHIFT_LEFT = "x1".U
      val SHIFT_RIGHT = "x5".U
      val SLT = "x2".U
      val SLTU = "x3".U

      // Assign input values
      a := io.input.data1
      b := io.input.imm

      // Perform calculations
      switch(io.input.funct3) {
        is(ADD_SUB) {
          io.result := a + b
        }
        is(XOR) {
          io.result := a ^ b
        }
        is(OR) {
          io.result := a | b
        }
        is(AND) {
          io.result := a & b
        }
        is(SHIFT_LEFT) {
          when(b(11, 5) === "x00".U) {
            io.result := Mux(b >= 32.S, 0.S, a << b(4,0).asUInt)
          } .otherwise {
            io.result := ERROR
          }
        }
        is(SHIFT_RIGHT) {
          when(b(11, 5) === "x00".U) {
            io.result := (a.asUInt >> b.asUInt).asSInt
          } .elsewhen(b(11, 5) === "x20".U) {
            io.result := a >> b.asUInt
          } .otherwise {
            io.result := ERROR
          }
        }
        is(SLT) {
          io.result := Mux(a < b, 1.S(32.W), 0.S(32.W))
        }
        is(SLTU) {
          io.result := Mux(a.asUInt < b.asUInt, 1.S(32.W), 0.S(32.W))
        }
      }
    }

    is(I_Type_2, I_Type_3, S_Type) {
      io.result := io.input.data1 + io.input.imm
    }

    is(I_Type_4) {
      // ecall/ebreak
    }


    is(B_Type) {
      val BEQ = "x0".U
      val BNE = "x1".U
      val BLT = "x4".U
      val BGE = "x5".U
      val BLTU = "x6".U
      val BGEU = "x7".U

      a := io.input.data1
      b := io.input.data2

      switch(io.input.funct3) {
        is(BEQ) {
          io.check := a === b
        }
        is(BNE) {
          io.check := a =/= b
        }
        is(BLT) {
          io.check := a < b
        }
        is(BGE) {
          io.check := a >= b
        }
        is(BLTU) {
          io.check := a.asUInt < b.asUInt
        }
        is(BGEU) {
          io.check := a.asUInt >= b.asUInt
        }
      }
    }

    is(U_Type_1, U_Type_2) {
      a := io.input.imm

      io.result := a << 12
    }

    is(J_Type) {
      // JUMP AND LINK
    }
  }
}
