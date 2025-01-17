import chisel3._
import chisel3.util._

class RegisterFile(fpga: Boolean) extends Module {
  val io = IO(new Bundle{
    val rs1 = Input(UInt(5.W))
    val rs2 = Input(UInt(5.W))
    val rd = Input(UInt(5.W))
    val data_in = Input(SInt(32.W))
    val write_enable = Input(Bool())
    val data1 = Output(SInt(32.W))
    val data2 = Output(SInt(32.W))
  })

  // Registers
  val regs = RegInit(VecInit(Seq.fill(32)(0.S(32.W))))

  // Reading from registers
  io.data1 := RegNext(Mux(io.rs1 === 0.U, 0.S, regs(io.rs1)).asSInt)
  io.data2 := RegNext(Mux(io.rs2 === 0.U, 0.S, regs(io.rs2)).asSInt)

  // Writing to registers
  when (io.rd =/= 0.U && io.write_enable) {
    regs(io.rd) := io.data_in
  }
}
