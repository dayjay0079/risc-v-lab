import chisel3._
import chisel3.util._
import lib.ControlBus

// TODO
// Write-enable for registers (ID stage) and memory (MEM stage)
// memTOReg boolean for write-back

class Control extends Module{
  val io = IO(new Bundle{
    val instruction = Input(UInt(32.W))
    val rs1 = Output(UInt(5.W))
    val rs2 = Output(UInt(5.W))
    val rd = Output(UInt(5.W))
    val imm = Output(SInt(32.W))
    val ctrl = Output(new ControlBus)
  })

  // Default Output values
  val rd = WireDefault(0.U(5.W))
  val rs1 = WireDefault(0.U(5.W))
  val rs2 = WireDefault(0.U(5.W))
  val imm = WireDefault(0.S(32.W))
  val opcode = io.instruction(6, 0)
  val funct3 = io.instruction(14, 12)
  val funct7 = io.instruction(31, 25)

  // Control values
  val inst_type = WireDefault(0.U(5.W))
  val write_enable_reg = WireDefault(1.B)
  val mem_store_type = Wire(UInt(2.W))
  val mem_load_type = Wire(UInt(3.W))
  val mem_to_reg = WireDefault(0.B)

  // I/O Connections
  io.rd := rd
  io.rs1 := rs1
  io.rs2 := rs2
  io.imm := imm
  io.ctrl.pc := DontCare
  io.ctrl.opcode := opcode
  io.ctrl.funct3 := funct3
  io.ctrl.funct7 := funct7
  io.ctrl.inst_type := inst_type
  io.ctrl.write_enable_reg := write_enable_reg
  io.ctrl.store_type := mem_store_type
  io.ctrl.load_type := mem_load_type
  io.ctrl.mem_to_reg := mem_to_reg

  //Enumeration of Instruction Types
  object InstructionType extends ChiselEnum {
    val NaI, ADD, SUB, XOR, OR, AND, SLL, SRL, SRA, SLT, SLTU,
    BEQ, BNE, BLT, BGE, BLTU, BGEU, JAL, LUI, AUIPC = Value
  }
  import InstructionType._

  object SType extends ChiselEnum {
    val NS, SB, SH, SW = Value
  }
  import SType._

  object LType extends ChiselEnum {
    val NL, LB, LH, LW, LB_U, LH_U = Value
  }
  import LType._

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

  // Assign register values depending on instruction type
  switch(opcode) {
    is(R_Type) {
      rd := io.instruction(11, 7)
      rs1 := io.instruction(19, 15)
      rs2 := io.instruction(24, 20)
    }
    is(I_Type_1, I_Type_2, I_Type_3) {
      rd := io.instruction(11, 7)
      rs1 := io.instruction(19, 15)
    }
    is(S_Type, B_Type) {
      rs1 := io.instruction(19, 15)
      rs2 := io.instruction(24, 20)
    }
    is(U_Type_1, U_Type_2, J_Type) {
      rd := io.instruction(11, 7)
    }
  }

  //Assign immediate values depending on instruction type
  switch(opcode) {
    is(I_Type_1) {
      imm := io.instruction(31, 20).asSInt
    }
    is(I_Type_2) {
      imm := io.instruction(31, 20).asSInt
      mem_to_reg := 1.B
    }
    is(I_Type_3) {
      imm := io.instruction(31, 20).asSInt
    }
    is(S_Type) {
      imm := Cat(io.instruction(31, 25), io.instruction(11, 7)).asSInt
      write_enable_reg := 0.B
    }
    is(B_Type) {
      imm := Cat(io.instruction(31), io.instruction(7), io.instruction(30, 25), io.instruction(11, 8)).asSInt << 1
      write_enable_reg := 0.B
    }
    is(U_Type_1) {
      imm := (Cat(io.instruction(31, 12), 0.U(12.W))).asSInt
    }
    is(U_Type_2) {
      imm := (Cat(io.instruction(31, 12), 0.U(12.W))).asSInt
    }
    is(J_Type) {
      imm := Cat(io.instruction(31), io.instruction(19, 12),  io.instruction(20), io.instruction(30, 21), 0.U(1.W)).asSInt
    }
  }

  //Choose Instruction Type
  inst_type := NaI.asUInt //Default instruction "Not an Instruction" zeroes outputs
  mem_store_type := NS.asUInt // Default value "No Store" for stage 4
  mem_load_type := NL.asUInt // Default value "No Load" for stage 4
  switch(funct3) {
    is("x0".U) {
      switch(opcode) {
        is(R_Type) { inst_type := Mux(funct7_type00, ADD.asUInt, Mux(funct7_type20, SUB.asUInt, NaI.asUInt)) }
        is(I_Type_1) { inst_type := ADD.asUInt }
        is(I_Type_2) { inst_type := ADD.asUInt; mem_load_type := LB.asUInt }
        is(I_Type_3) { inst_type := JAL.asUInt }
        is(S_Type) { inst_type := ADD.asUInt; mem_store_type := SB.asUInt}
        is(B_Type) { inst_type := BEQ.asUInt }
        is(U_Type_1) { inst_type := LUI.asUInt }
        is(U_Type_2) { inst_type := AUIPC.asUInt }
        is(J_Type) {inst_type := JAL.asUInt}
      }
    }
    is("x1".U) {
      switch(opcode) {
        is(R_Type) { inst_type := Mux(funct7_type00, SLL.asUInt, NaI.asUInt) }
        is(I_Type_1) { inst_type := Mux(imm_type00, SLL.asUInt, NaI.asUInt) }
        is(I_Type_2) { inst_type := ADD.asUInt; mem_load_type := LH.asUInt }
        is(S_Type) { inst_type := ADD.asUInt; mem_store_type := SH.asUInt}
        is(B_Type) { inst_type := BNE.asUInt }
        is(U_Type_1) { inst_type := LUI.asUInt }
        is(U_Type_2) { inst_type := AUIPC.asUInt }
        is(J_Type) {inst_type := JAL.asUInt}
      }
    }
    is("x2".U) {
      switch(opcode) {
        is(R_Type) { inst_type := Mux(funct7_type00, SLT.asUInt, NaI.asUInt) }
        is(I_Type_1) { inst_type := SLT.asUInt }
        is(I_Type_2) { inst_type := ADD.asUInt; mem_load_type := LW.asUInt }
        is(S_Type) { inst_type := ADD.asUInt; mem_store_type := SW.asUInt}
        is(U_Type_1) { inst_type := LUI.asUInt }
        is(U_Type_2) { inst_type := AUIPC.asUInt }
        is(J_Type) {inst_type := JAL.asUInt}
      }
    }
    is("x3".U) {
      switch(opcode) {
        is(R_Type) { inst_type := Mux(funct7_type00, SLTU.asUInt, NaI.asUInt) }
        is(I_Type_1) { inst_type := SLTU.asUInt }
        is(U_Type_1) { inst_type := LUI.asUInt }
        is(U_Type_2) { inst_type := AUIPC.asUInt }
        is(J_Type) {inst_type := JAL.asUInt}
      }
    }
    is("x4".U) {
      switch(opcode) {
        is(R_Type) { inst_type := Mux(funct7_type00, XOR.asUInt, NaI.asUInt) }
        is(I_Type_1) { inst_type := XOR.asUInt }
        is(I_Type_2) { inst_type := ADD.asUInt; mem_load_type := LB_U.asUInt }
        is(B_Type) { inst_type := BLT.asUInt }
        is(U_Type_1) { inst_type := LUI.asUInt }
        is(U_Type_2) { inst_type := AUIPC.asUInt }
        is(J_Type) {inst_type := JAL.asUInt}
      }
    }
    is("x5".U) {
      switch(opcode) {
        is(R_Type) { inst_type := Mux(funct7_type00, SRL.asUInt, Mux(funct7_type20, SRA.asUInt, NaI.asUInt)) }
        is(I_Type_1) { inst_type := Mux(imm_type00, SRL.asUInt, Mux(imm_type20, SRA.asUInt, NaI.asUInt)) }
        is(I_Type_2) { inst_type := ADD.asUInt; mem_load_type := LH_U.asUInt }
        is(B_Type) { inst_type := BGE.asUInt }
        is(U_Type_1) { inst_type := LUI.asUInt }
        is(U_Type_2) { inst_type := AUIPC.asUInt }
        is(J_Type) {inst_type := JAL.asUInt}
      }
    }
    is("x6".U) {
      switch(opcode) {
        is(R_Type) { inst_type := Mux(funct7_type00, OR.asUInt, NaI.asUInt) }
        is(I_Type_1) { inst_type := OR.asUInt }
        is(B_Type) { inst_type := BLTU.asUInt }
        is(U_Type_1) { inst_type := LUI.asUInt }
        is(U_Type_2) { inst_type := AUIPC.asUInt }
        is(J_Type) {inst_type := JAL.asUInt}
      }
    }
    is("x7".U) {
      switch(opcode) {
        is(R_Type) { inst_type := Mux(funct7_type00, AND.asUInt, NaI.asUInt) }
        is(I_Type_1) { inst_type := AND.asUInt }
        is(B_Type) { inst_type := BGEU.asUInt }
        is(U_Type_1) { inst_type := LUI.asUInt }
        is(U_Type_2) { inst_type := AUIPC.asUInt }
        is(J_Type) {inst_type := JAL.asUInt}
      }
    }
  }
}
